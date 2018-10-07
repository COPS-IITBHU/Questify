package in.shriyansh.streamify.activities;

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
import in.shriyansh.streamify.utils.PreferenceUtils;
import needle.Needle;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CreateEventNotif extends AppCompatActivity {

    @BindView(R.id.progress_create_event_notif)    ProgressBar progressBar;
    @BindView(R.id.fab_create_event_notif)         FloatingActionButton fab;
    @BindView(R.id.create_event_title)                    EditText title;
    @BindView(R.id.create_event_content)              EditText description;
    @BindView(R.id.stream_box_group)               LinearLayout checkbox_layout;
    private List<String> all_streams;
    private List<String> checked_streams;
    private CheckBox[] streambox;
    public static final String TAG = "CreateEventNotif.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_notif);
        ButterKnife.bind(this);   // initUI

        all_streams = new ArrayList<>();
        checked_streams = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        getStreams(); // will call createStreamCheckboxes() if successful

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = title.getText().toString();
                String des = description.getText().toString();
                if (!t.contentEquals("") && !des.contentEquals(""))
                    post(t, des);
                else
                    Snackbar.make(findViewById(R.id.container_create_event_notif),
                            R.string.snackbar_specify_title_content_for_post, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    void post(final String title, String description) {
        
        // TODO Sending empty imageURL, subtitle and location for now
        // TODO Either remove them from API or add edittexts in the app
        
        // TODO Skipping tags for now
        // TODO Skipping streams for now
        
        String imageURL = "";
        String subtitle = "";
        String location = "";
        String authorEmail = PreferenceUtils.getStringPreference(CreateEventNotif.this, PreferenceUtils.PREF_USER_EMAIL);
        
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{\n\t\"title\":\""+title+"\"," +
                "\n\t\"description\":\""+description+"\"," +
                "\n\t\"imageURL\":\""+imageURL+"\"," +
                "\n\t\"location\":\""+location+"\"," +
                "\n\t\"subtitle\":\""+subtitle+"\"," +
                "\n\t\"authorEmail\":\""+authorEmail+"\"," +
                "\n\t\"streams\":[\n\t\t\t\"MyStream\"\n\t\t]," +
                "\n\t\"tags\":[\n\t\t\t\"Noobs\",\n\t\t\t\"Python\"\n\t\t]\n}"
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
    private void getStreams() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Urls.LIST_ALL_STREAMS)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                // on failure response
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(findViewById(R.id.container_create_event_notif),
                                "onFailure: " + e.toString(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // get stream titles
                // subscribe to them when user hits save

                try {
                    String jsonData = response.body().string();
                    JSONObject object = new JSONObject(jsonData);

                    String status = object.getString("status");
                    if (status.equals("200")) {
                        // get stream titles
                        JSONArray array = object.getJSONArray("response");

                        // create a list
                        for (int i = 0; i < array.length(); ++i)
                            all_streams.add(array.getJSONObject(i).getString("title"));

                        createStreamCheckboxes();
                    } else {
                        throw new Exception("onResponse Status: " + status);
                    }
                } catch (final Exception e) {
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(R.id.container_create_event_notif),
                                    "onFailure: " + e.toString(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void createStreamCheckboxes() {
        String res = "";
        for(String s:all_streams)
            res.concat(s+";");
        Log.d(TAG,res);   // print all streams

        streambox = new CheckBox[all_streams.size()];

        for (int i = 0; i < all_streams.size(); i++) {
            streambox[i] = new CheckBox(CreateEventNotif.this);
            streambox[i].setText(all_streams.get(i));
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
}
