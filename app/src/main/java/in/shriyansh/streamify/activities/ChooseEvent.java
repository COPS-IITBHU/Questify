package in.shriyansh.streamify.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import in.shriyansh.streamify.utils.PreferenceUtils;

import static in.shriyansh.streamify.network.Urls.LIST_ALL_EVENTS;

public class ChooseEvent extends AppCompatActivity {

    private RadioGroup events_radio;
    private String[] events;
    private RadioButton[] event;
    private Button btn_reg_event;
    private LinearLayout progress_layout;

    private String[] available_events;
    private String[] corresponding_event_ids;

    private RequestQueue volleyQueue;

    private final String TAG = "ChooseEventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        volleyQueue = Volley.newRequestQueue(this);

        events_radio = findViewById(R.id.events_radio);
        events = getResources().getStringArray(R.array.events_array);
//        progress_layout = findViewById(R.id.layout_progress_choose_event);

        events_radio.setVisibility(View.GONE);
        progress_layout.setVisibility(View.VISIBLE);

        getEvents(new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                available_events = new String[result.length()];
                corresponding_event_ids = new String[result.length()];

                try {
                    for (int i = 0; i < result.length(); i++) {
                        available_events[i] = result.getJSONObject(i).getString("title");
                        corresponding_event_ids[i] = result.getJSONObject(i).getString("id");
                    }

                    Log.e(TAG, available_events[0]);
                    event = new RadioButton[available_events.length];

                    for(int i=0; i<available_events.length; i++) {
                        event[i] = new RadioButton(ChooseEvent.this);
                        event[i].setText(available_events[i]);
                        event[i].setId(i);
                        event[i].setTextColor(Color.WHITE);
                        event[i].setTextSize(getResources().getDimension(R.dimen.radioTextSize));
                        events_radio.addView(event[i]);
                    }

                    set_selected_event_id(corresponding_event_ids, events_radio);

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        events_radio.setVisibility(View.VISIBLE);
        progress_layout.setVisibility(View.GONE);

    }

    private void set_selected_event_id(final String[] corresp_event_ids, final RadioGroup events_radiogrp) {

        btn_reg_event = findViewById(R.id.btn_event_reg);
        btn_reg_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String event_id = corresp_event_ids[events_radiogrp.getCheckedRadioButtonId()];
                PreferenceUtils.setStringPreference(ChooseEvent.this, PreferenceUtils.PREF_USER_EVENT, event_id);
                Log.e(TAG, event_id);

                Intent intent = new Intent(ChooseEvent.this, ChooseNumberMembers.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void getEvents(final VolleyCallback callback) {

        //Get All Events

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET,
                LIST_ALL_EVENTS, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                Log.e(TAG, resp.toString());
                try {
                    String status = resp.getString(Constants.RESPONSE_STATUS_KEY);
                    if (status.equals(Constants.RESPONSE_STATUS_VALUE_200)) {

                        JSONArray jsonArray = resp.getJSONArray("response");

                        callback.onSuccess(jsonArray);

                    }
                    else {
                        Toast.makeText(ChooseEvent.this, "Cannot fetch events:( Try Again later", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.toString());
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.HTTP_INITIAL_TIME_OUT,
                Constants.HTTP_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        volleyQueue.add(stringRequest);

    }

    private interface VolleyCallback {
        void onSuccess(JSONArray result);
    }

}
