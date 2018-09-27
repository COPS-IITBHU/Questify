

/**
 * THE ACTIVITY TO SEND NOTIFICATION DETAILS
**/

package in.shriyansh.streamify.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.utils.Constants;
//import needle.Needle;
//import needle.UiRelatedProgressTask;
//import needle.UiRelatedTask;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.RequestBody;
import static in.shriyansh.streamify.network.Urls.EVENT_NOTIFICATION_URL;
import static in.shriyansh.streamify.network.Urls.LIST_ALL_STREAMS;


public class CreateEventNotif extends AppCompatActivity {

    private Button reg_event;

    private static final String TAG = "CreateEventNotif";
    private String[] streams;
    private String httpstatus;
    private CheckBox[] streambox;
    private int[] streamCheckBools;
    private LinearLayout checkbox_layout;

    private RequestQueue requestQueue;

    private int loop_var_box;
    private int l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        requestQueue = Volley.newRequestQueue(CreateEventNotif.this);

        initUI();
        getStreams();
        Log.e(TAG, streams.toString());
//        createStreamCheckboxes();

        reg_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject params = new JSONObject();
                try {
                    params.put("title", "MyEvent9");
                    params.put("description", "MyDescription");
                    params.put("imageURL", "");
                    params.put("location", "G-12 IITBHU");
                    params.put("authorEmail", "akshay.sharma.mat16@iitbhu.ac.in");

                    JSONArray streamArray = new JSONArray();
                    JSONArray tagArray = new JSONArray();

                    streamArray.put("MyStream");
                    tagArray.put("Boobs");
                    tagArray.put("python");

                    params.put("streams", streamArray);
                    params.put("tags", tagArray);

                    Log.e(TAG, params.toString());

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                            EVENT_NOTIFICATION_URL,
                            params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        response.getString("status");
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(CreateEventNotif.this, "Response Error!!", Toast.LENGTH_LONG).show();
                                }
                            }){
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put(Constants.HTTP_HEADER_CONTENT_TYPE_KEY,
                                            Constants.HTTP_HEADER_CONTENT_TYPE_JSON);
                                    return headers;
                                }
                            };

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    Constants.HTTP_INITIAL_TIME_OUT,
                    Constants.HTTP_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


                    requestQueue.add(jsonObjectRequest);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

//
//                Needle.onBackgroundThread().execute(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        OkHttpClient client = new OkHttpClient();
//
//                        MediaType JSON
//                                = MediaType.parse("application/json; charset=utf-8");
//
//                        /*************************************************************/
//
//                        RequestBody body = RequestBody.create(JSON, "{\n\t\"title\":\"MyEvent\"," +
//                                "\n\t\"description\":\"MyDescription\"," +
//                                "\n\t\"imageURL\":\"\"," +
//                                "\n\t\"location\":\"G-11 IIT BHU\"," +
//                                "\n\t\"authorEmail\":\"akshay.sharma.mat16@iitbhu.ac.in\"," +
//                                "\n\t\"streams\":[\n\t\t\t\"MyStream\"\n\t\t]," +
//                                "\n\t\"tags\":[\n\t\t\t\"Noobs\",\n\t\t\t\"Python\"\n\t\t]\n}"
//                        );
//
//                        okhttp3.Request request = new okhttp3.Request.Builder()
//                                .url(EVENT_NOTIFICATION_URL)
//                                .post(body)
//                                .addHeader("Content-Type", "application/json")
//                                .addHeader("Cache-Control", "no-cache")
//                                .addHeader("Postman-Token", "faa6d8dd-3b5c-420b-ba43-9eb1d413a7a6")
//                                .build();
//
//                        try {
//                            okhttp3.Response response = client.newCall(request).execute();
//                            Log.e(TAG, response.body().toString());
//                            JSONObject object;
//                            try {
//                                object=new JSONObject(response.body().string());
//                                httpstatus = object.getString("status");
//                                Log.e(TAG, httpstatus);
//                            }
//                            catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                });


                    }
                });

            }

    private void initUI() {
        reg_event = findViewById(R.id.btn_create_event1);
        checkbox_layout = findViewById(R.id.stream_box_group);
    }

    private void getStreams() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                LIST_ALL_STREAMS,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals(Constants.RESPONSE_STATUS_VALUE_200)) {
                                l = response.getJSONArray("response").length();
                                JSONArray resp = response.getJSONArray("response");
                                streams = new String[l];

                                for (int i = 0; i<l; i++) {
                                    JSONObject user_stream_i = resp.getJSONObject(i);
                                    streams[i] = user_stream_i.getString("title");
//                                    Log.e(TAG, streams[i]);
                                }

                                Log.e(TAG, streams[0]);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSONException");
                        }
                    }
                },
                new Response.ErrorListener() {
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
/*******************************************************************/

//        Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String, String>() {
//            int i=0;
//
//            @Override
//            protected void onProgressUpdate(String s) {
//                Log.e(TAG, s);
//                streams[i] = s;
//                i++;
//
//
//            }
//
//            @Override
//            public String doWork() {
//                OkHttpClient client1 = new OkHttpClient();
//                String streams1;
//                int l = 0;
//
//                okhttp3.Request request = new okhttp3.Request.Builder()
//                        .url(LIST_ALL_STREAMS)
//                        .build();
//
//                try {
//                    okhttp3.Response response = client1.newCall(request).execute();
//                    Log.e(TAG, response.body().toString());
//                try {
//                    JSONObject object;
//                    object = new JSONObject(response.body().string());
//                    httpstatus = object.getString("status");
//
//                    l = object.getJSONArray("response").length();
//                    streams = new String[l];
//
//                    for (int i=0; i<l; i++) {
//                        streams1 = object.getJSONArray("response").getJSONObject(i).getString("title");
////                        streams[i] = streams1;
//                        publishProgress(streams1);
//                    }
//                }
//                catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                return Integer.toString(l);
//            }
//
//            @Override
//            protected void thenDoUiRelatedWork(String s) {
//                Toast.makeText(CreateEventNotif.this, "scnnlls" + s, Toast.LENGTH_LONG).show();
//            }
//
//
//        });

    }

    private void createStreamCheckboxes() {

        streambox = new CheckBox[streams.length];
        streamCheckBools = new int[streams.length];

        for (loop_var_box=0; loop_var_box<streams.length; loop_var_box++) {
            streambox[loop_var_box] = new CheckBox(CreateEventNotif.this);
            streambox[loop_var_box].setText(streams[loop_var_box]);
            streambox[loop_var_box].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            streambox[loop_var_box].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    streamCheckBools[loop_var_box] = (isChecked ? 1 : 0);
                }
            });

            checkbox_layout.addView(streambox[loop_var_box]);
        }

    }


}
