package in.shriyansh.streamify.activities;

import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.shriyansh.streamify.R;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;
import needle.Needle;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static in.shriyansh.streamify.network.Urls.LIST_ALL_STREAMS;


public class CreateEventNotif extends AppCompatActivity {

    @BindView(R.id.progress_create_event_notif)    ProgressBar progressBar;
    @BindView(R.id.fab_create_event_notif)         FloatingActionButton fab;
    @BindView(R.id.event_title)                    EditText title;
    @BindView(R.id.event_description)              EditText description;
    @BindView(R.id.stream_box_group)               LinearLayout checkbox_layout;
    @BindView(R.id.tag_box_group)                  LinearLayout tag_layout;
    @BindView(R.id.event_subtitle)                 EditText et_subtitle;
    @BindView(R.id.event_image)                    EditText et_imageURL;
    @BindView(R.id.event_location)                 EditText et_location;

    private List<String> all_streams;
    private List<String> checked_streams;
    private List<String> checked_tags;
    private CheckBox[] streambox;
    private CheckBox[] tagbox;
    public static final String TAG = "CreateEventNotif.java";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_notif);
        requestQueue = Volley.newRequestQueue(CreateEventNotif.this);

        ButterKnife.bind(this);   // initUI

        all_streams = new ArrayList<>();
        checked_streams = new ArrayList<>();
        checked_tags = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);

        //Using VolleyCallback since volley cannot modify layout from
        // inside the volley onResponse
        getStreams(new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                int l = result.length();
                String[] streams = new String[l];
                try {
                    for (int i = 0; i < l; i++) {
                        JSONObject user_stream_i = result.getJSONObject(i);
                        streams[i] = user_stream_i.getString("title");
                    }

                    createStreamCheckboxes(streams);

                    //TODO Use a standard set of tags either served or predefined in the app
                    /****************************************************/
                    /*predefined tags*/

                    String[] tags;

                    tags = new String[5];
                    tags[0] = "Competitive Programming";
                    tags[1] = "Machine Learning";
                    tags[2] = "Development";
                    tags[3] = "Open Source";
                    tags[4] = "Robotics";

                    /****************************************************/


                    createTagboxes(tags);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        // will call createStreamCheckboxes() if successful



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = title.getText().toString();
                String des = description.getText().toString();
                String subtitle = et_subtitle.getText().toString();
                String location = et_location.getText().toString();
                String imageURL = et_imageURL.getText().toString();

                if (!t.contentEquals("") && !des.contentEquals("")) {
                    post(t, des, subtitle, location, imageURL);
                }
                else
                    Snackbar.make(findViewById(R.id.container_create_event_notif),
                            R.string.snackbar_specify_title_content_for_post, Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void createTagboxes(String[] tags) {
        tagbox = new CheckBox[tags.length];

        for (int loop_var_box=0; loop_var_box<tags.length; loop_var_box++) {
            tagbox[loop_var_box] = new CheckBox(CreateEventNotif.this);
            tagbox[loop_var_box].setText(tags[loop_var_box]);
            tagbox[loop_var_box].setTextColor(Color.GRAY);
            tagbox[loop_var_box].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            streambox[loop_var_box].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        checked_tags.add(buttonView.getText().toString());
                    else
                        checked_tags.remove(buttonView.getText().toString());
                }
            });


            tag_layout.addView(tagbox[loop_var_box]);
        }

    }


    void post(final String title, String description, String imageURL, String subtitle, String location) {
        
        // TODO Sending empty imageURL, subtitle and location for now
        // TODO Either remove them from API or add edittexts in the app
        
        // TODO Skipping tags for now
        // TODO Skipping streams for now
        
        String authorEmail = PreferenceUtils.getStringPreference(CreateEventNotif.this, PreferenceUtils.PREF_USER_EMAIL);
        
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        // TODO something aint right here, gives 400
        RequestBody body = RequestBody.create(JSON, "{\n\t\"title\":\""+title+"\"," +
                "\n\t\"description\":\""+description+"\"," +
                "\n\t\"imageURL\":\""+imageURL+"\"," +
                "\n\t\"location\":\""+location+"\"," +
                "\n\t\"subtitle\":\""+subtitle+"\"," +
                "\n\t\"authorEmail\":\""+authorEmail+"\"," +
                "\n\t\"streams\":\n\t\t\t\"" + checked_streams + "\"\n\t\t," +
                "\n\t\"tags\":\n\t\t\t\"" + checked_tags + "\"\n\t\t\n}"
        );


        Request request = new Request.Builder()
                .url(Urls.EVENT_NOTIFICATION_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        progressBar.setVisibility(View.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "postEvent method failed");
                e.printStackTrace();
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(findViewById(R.id.container_create_event_notif),
                                R.string.snackbar_error, Snackbar.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d(TAG, response.body().string());
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(findViewById(R.id.container_create_event_notif),
                                R.string.snackbar_thankyou_for_post, Snackbar.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    // get all streams to fill the checkboxes
    // call createStreamCheckboxes() when done
    private void getStreams(final VolleyCallback callback) {

        //Using Volley for testing as Needle was not suited for the callback thing
        //probably

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET,
                LIST_ALL_STREAMS,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals(Constants.RESPONSE_STATUS_VALUE_200)) {
                                JSONArray resp = response.getJSONArray("response");

                                callback.onSuccess(resp);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSONException");
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.toString());

                    }
                });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.HTTP_INITIAL_TIME_OUT,
                Constants.HTTP_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);






//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url(Urls.LIST_ALL_STREAMS)
//                .get()
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, final IOException e) {
//                // on failure response
//                Needle.onMainThread().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        progressBar.setVisibility(View.GONE);
//                        Snackbar.make(findViewById(R.id.container_create_event_notif),
//                                "onFailure: " + e.toString(), Snackbar.LENGTH_LONG).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // get stream titles
//                // subscribe to them when user hits save
//
//                try {
//                    String jsonData = response.body().string();
//                    JSONObject object = new JSONObject(jsonData);
//
//                    String status = object.getString("status");
//                    if (status.equals("200")) {
//                        // get stream titles
//                        JSONArray array = object.getJSONArray("response");
//                        Log.e(TAG, array.toString());
//
//                        callback.onSuccess(array);
//
//                        // create a list
////                        for (int i = 0; i < array.length(); ++i)
////                            all_streams.add(array.getJSONObject(i).getString("title"));
////
////                        createStreamCheckboxes();
//                    } else {
//                        throw new Exception("onResponse Status: " + status);
//                    }
//                } catch (final Exception e) {
//                    Needle.onMainThread().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressBar.setVisibility(View.GONE);
//                            Snackbar.make(findViewById(R.id.container_create_event_notif),
//                                    "onFailure: " + e.toString(), Snackbar.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            }
//        });
    }

    private void createStreamCheckboxes(String[] all_streams) {
        String res = "";
        for(String s:all_streams)
            res.concat(s+";");
        Log.d(TAG,res);   // print all streams

        int l = all_streams.length;
        streambox = new CheckBox[l];

        for (int i = 0; i < l; i++) {
            streambox[i] = new CheckBox(CreateEventNotif.this);
            streambox[i].setText(all_streams[i]);
            streambox[i].setTextColor(Color.GRAY);
            streambox[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            streambox[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        checked_streams.add(buttonView.getText().toString());
                    else
                        checked_streams.remove(buttonView.getText().toString());
                }
            });

            checkbox_layout.addView(streambox[i]);
        }

        // Remember it's called by a background thread
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private interface VolleyCallback {
        void onSuccess(JSONArray result);
    }

}
