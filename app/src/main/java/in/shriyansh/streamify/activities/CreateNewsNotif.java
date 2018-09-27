package in.shriyansh.streamify.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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

import java.io.IOException;

public class CreateNewsNotif extends AppCompatActivity {
    private static final String TAG = CreateNewsNotif.class.getSimpleName();

    private EditText titleTv;
    private EditText contentTv;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_notif);

        initUi();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.GONE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = titleTv.getText().toString();
                final String content = contentTv.getText().toString();
                if (!title.contentEquals("") && !content.contentEquals("")) {
                    post(title, content);
                } else {
                    showSnackBar(R.string.snackbar_specify_title_content_for_post);
                }
            }
        });
    }

    /**
     * Initializes UI elements on view.
     */
    private void initUi() {
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        titleTv = findViewById(R.id.post_title);
        contentTv = findViewById(R.id.post_content);
        progressBar = findViewById(R.id.progress);
    }

    /**
     * Sends the post to server using volley.
     *
     * @param title   Title of the post
     * @param content Post Content
     */
    private void post(String title, String content) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"title\":\"" + title +
                "\",\n\t\"content\":\"" + content + "\",\n\t\"authorEmail\":\"" +
                PreferenceUtils.getStringPreference(
                        CreateNewsNotif.this, PreferenceUtils.PREF_USER_EMAIL) + "\"\n}");

        Request request = new Request.Builder()
                .url(Urls.NEWS_NOTIFICATION_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        progressBar.setVisibility(View.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "postNews method failed");
                e.printStackTrace();
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        showSnackBar(R.string.snackbar_error);
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
                        showSnackBar(R.string.snackbar_thankyou_for_post);
                        progressBar.setVisibility(View.GONE);
                        titleTv.setText("");
                        contentTv.setText("");
                    }
                });
            }
        });
    }

    private void showSnackBar(final int stringResource) {
        Snackbar.make(findViewById(R.id.container),
                getResources().getString(stringResource), Snackbar.LENGTH_LONG).show();
    }
}