package in.shriyansh.streamify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * User Post Activity.
 */
public class CreateNewsNotif extends AppCompatActivity {
    private static final String TAG = CreateNewsNotif.class.getSimpleName();

    private EditText titleTv;
    private EditText contentTv;
    private RadioGroup typeRd;
    private String type;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private RequestQueue volleyQueue;
    private LinearLayout contentLayout;
    private LinearLayout progressLayout;
    private ProgressBar progressBar;

    private static final String TYPE_NEWS = "News";
    private static final String TYPE_EVENT = "Event";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initUi();
        initToolbar();
        setPostFab();

        volleyQueue = Volley.newRequestQueue(this);
        type = TYPE_NEWS;
    }

    /**
     *  Initializes UI elements on view.
     */
    private void initUi() {
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        typeRd = findViewById(R.id.post_radio);
        titleTv = findViewById(R.id.post_title);
        contentTv = findViewById(R.id.post_content);
        contentLayout = findViewById(R.id.content_layout);
        progressLayout = findViewById(R.id.progress_layout);
        /*
         * Nothing worked for me while trying to change the color of progressbar in xml file
         *  android:progressBackgroundTint="@color/ColorPrimary"
         *  android:progressTint="@color/ColorPrimary"
         *  android:background="@color/ColorPrimary"
         *  android:backgroundTint="@color/ColorPrimary"
         *  only worked by java
         */
        progressBar = findViewById(R.id.progress);
        progressBar.setIndeterminate(true);
        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.ColorPrimary),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        typeRd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radio_news) {
                    type = TYPE_NEWS;
                } else if (i == R.id.radio_event) {
                    type = TYPE_EVENT;
                }
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Sends the post to server using volley.
     *
     * @param title     Title of the post
     * @param type      News or Event
     * @param content   Post Content
     * @param userId    User Id of the creator
     */
    private void post(String title,String type,String content,String userId) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.POST_PARAM_USER_ID,userId);
        params.put(Constants.POST_PARAM_TYPE,type);
        params.put(Constants.POST_PARAM_TITLE,title);
        params.put(Constants.POST_PARAM_CONTENT,content);
        Log.d(TAG,params.toString());

        toggleProgressLayout();

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                Urls.POST, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                Log.d(TAG, resp.toString());
                try {
                    String status = resp.getString(Constants.RESPONSE_STATUS_KEY);
                    if (status.equals(Constants.RESPONSE_STATUS_VALUE_OK)) {
                        showSnackBar(R.string.snackbar_thankyou_for_post);
                        titleTv.setText("");
                        contentTv.setText("");
                    } else {
                        showSnackBar(R.string.snackbar_error);
                    }
                    toggleContentLayout();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showSnackBarWithWirelessSetting(R.string.snackbar_cannot_reach_servers);
                    toggleContentLayout();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.toString());
                showSnackBarWithWirelessSetting(R.string.snackbar_cannot_reach_servers);
                toggleContentLayout();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.HTTP_HEADER_CONTENT_TYPE_KEY,
                        Constants.HTTP_HEADER_CONTENT_TYPE_JSON);
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.HTTP_INITIAL_TIME_OUT,
                Constants.HTTP_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        volleyQueue.add(stringRequest);
    }

    private void setPostFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleTv.getText().toString();
                String content = contentTv.getText().toString();
                if (!title.contentEquals("") && !content.contentEquals("")) {
                    post(title,type,content, PreferenceUtils.getStringPreference(
                            CreateNewsNotif.this,PreferenceUtils.PREF_USER_GLOBAL_ID));
                } else {
                    showSnackBar(R.string.snackbar_specify_title_content_for_post);
                }
            }
        });
    }

    /**
     * Shows Snackbar without any action button.
     *
     * @param stringResource Resource id for string to be shown on snackbar
     */
    private void showSnackBar(final int stringResource) {
        Snackbar.make(findViewById(R.id.container),
                getResources().getString(stringResource), Snackbar.LENGTH_LONG).show();
    }

    /**
     * Shows Snackbar with Action button for wireless settings.
     *
     * @param stringResource Resource id for string to be shown on snackbar
     */
    private void showSnackBarWithWirelessSetting(final int stringResource) {
        Snackbar.make(findViewById(R.id.container),
                getResources().getString(stringResource), Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(R.string.snackbar_action_settings),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        })
                .show();
    }

    private void toggleContentLayout() {
        contentLayout.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
    }

    private void toggleProgressLayout() {
        contentLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
    }
}