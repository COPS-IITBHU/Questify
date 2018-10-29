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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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


public class CreateEventNotif extends AppCompatActivity {

    @BindView(R.id.progress_create_event_notif)    ProgressBar progressBar;
    @BindView(R.id.fab_create_event_notif)         FloatingActionButton fab;
    @BindView(R.id.event_title)                    EditText title;
    @BindView(R.id.event_description)              EditText description;
    @BindView(R.id.stream_box_group)               LinearLayout checkbox_layout;
    @BindView(R.id.tag_box)                        EditText et_tags;
    @BindView(R.id.event_image)                    EditText et_imageURL;
    @BindView(R.id.event_location)                 EditText et_location;

    private List<String> checked_streams;
    private List<String> checked_tags;
    public static final String TAG = "CreateEventNotif.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_notif);
        ButterKnife.bind(this);   // initUI

        checked_streams = new ArrayList<>();
        checked_tags = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);

        createStreamCheckboxes();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titl = title.getText().toString();
                String des = description.getText().toString();
                String location = et_location.getText().toString();
                String tags = et_tags.getText().toString();
                String imageURL = et_imageURL.getText().toString();

                if (!titl.contentEquals("") && !des.contentEquals("") && !tags.contentEquals("")) {
                    post(titl, des, tags, location, imageURL);
                }
                else
                    Snackbar.make(findViewById(R.id.container_create_event_notif),
                            R.string.snackbar_specify_title_content_for_post, Snackbar.LENGTH_LONG).show();
            }
        });
    }


    void post(final String title, String description, String tags, String location,String imageURL) {
        
        String authorEmail = PreferenceUtils.getStringPreference(CreateEventNotif.this, PreferenceUtils.PREF_USER_EMAIL);
        
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        checked_streams = convertToQuoted(checked_streams);

        checked_tags = convertToQuoted(Arrays.asList(tags.split("\\s*,\\s*")));

        String json = "{\n\t\"title\":\""+title+"\"," +
                "\n\t\"description\":\""+description+"\"," +
                "\n\t\"imageURL\":\""+imageURL+"\"," +
                "\n\t\"location\":\""+location+"\"," +
                "\n\t\"authorEmail\":\""+authorEmail+"\"," +
                "\n\t\"streams\":" + checked_streams + "," +
                "\n\t\"tags\":" + checked_tags + "\n}";

        RequestBody body = RequestBody.create(JSON, json);

        Log.d(TAG, json);

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

    private List<String> convertToQuoted(List<String> checked_fields) {

        for (int i = 0; i < checked_fields.size(); i++) {
            String stream;
            stream = "\"" + checked_fields.get(i) + "\"";
            checked_fields.set(i, stream);
        }

        return checked_fields;

    }

    private void createStreamCheckboxes() {
        String[] all_streams = new String[]{"AERO","ASTRO","BIZ","COPS","CSI","ROBO","SAE"};

        int l = all_streams.length;
        CheckBox[] streambox = new CheckBox[l];

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
}
