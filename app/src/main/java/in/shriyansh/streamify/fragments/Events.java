package in.shriyansh.streamify.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.activities.EventDetailsActivity;
import in.shriyansh.streamify.adapters.EventsAdapter;
import in.shriyansh.streamify.database.DbContract;
import in.shriyansh.streamify.database.DbMethods;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment for event tab.
 *
 */
public class Events extends Fragment implements Urls {
    private static final String TAG = Events.class.getSimpleName();

    private SwipeRefreshLayout eventsRefreshLayout;
    private ListView eventsListView;

    private EventsAdapter eventsAdapter;

    private DbMethods dbMethods;
    private RequestQueue volleyQueue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbMethods = new DbMethods(getActivity());
        volleyQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && eventsAdapter != null) {
            eventsAdapter.changeCursor(dbMethods.queryEvents(null, null,
                null, DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 0));
            eventsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_layout,container,false);
        eventsListView = view.findViewById(R.id.events_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            eventsListView.setNestedScrollingEnabled(true);
        }
        eventsAdapter = new EventsAdapter(getActivity(),dbMethods.queryEvents(null,
                null, null,
                DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 0));
        eventsListView.setAdapter(eventsAdapter);
        eventsRefreshLayout = view.findViewById(R.id.news_refresh_layout);
        eventsRefreshLayout.setColorSchemeResources(R.color.ColorPrimary, R.color.pink500,
                R.color.teal500);

        /*
         * Registering Receiver
         */
        IntentFilter filter = new IntentFilter(Constants.DISPLAY_MESSAGE_ACTION);
        getActivity().getApplicationContext().registerReceiver(handleMessageReceiver, filter);

        /*
         * Implementing refresh
         */
        eventsRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            /*
             * make network call
             * and set setRefreshing(false); after refresh
             */
            getEvents(dbMethods.queryLastEventId() + "");
            }
        });

        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (l != 1) {
                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra(EventDetailsActivity.INTENT_EXTRA_KEY_EVENT_ID, l);
                startActivityForResult(intent, 2);
            }
            }
        });

        eventsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        eventsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private int nr = 0;

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                // Inflate the menu for the CAB
                nr = 0;
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.contextual_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                // Respond to clicks on the actions in the CAB
                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        nr = 0;
                        eventsAdapter.deleteSelected();
                        actionMode.finish();
                        return true;
                    case R.id.action_share:
                        nr = 0;
                        eventsAdapter.forwardSelected();
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                eventsAdapter.clearSelection();
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long id,
                                                  boolean checked) {
                if (checked) {
                    nr++;
                    eventsAdapter.setNewSelection(id, true);
                } else {
                    nr--;
                    eventsAdapter.removeSelection(id);
                }
                actionMode.setTitle(String.valueOf(nr));
            }
        });
        return view;
    }

    private void getEvents(String lastEventId) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.EVENT_PARAM_LAST_EVENT_ID,lastEventId);
        Log.d(TAG,params.toString());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                GET_EVENTS, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                Log.d(TAG, resp.toString());
                try {
                    String status = resp.getString(Constants.RESPONSE_STATUS_KEY);
                    if (status.equals(Constants.RESPONSE_STATUS_VALUE_200)) {
                        long count = dbMethods.insertEvents(resp.getJSONArray("response"));
                        eventsAdapter.changeCursor(dbMethods.queryEvents(null,
                                null, null,
                                DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 0));
                        eventsAdapter.notifyDataSetChanged();

                        String eventCount = count == 0 ? "No" : count + "";
                        if (isAdded()) {
                            showSnackBar(eventCount + " new Events.");
                        }
                    } else {
                        if (isAdded()) {
                            showSnackBar(R.string.snackbar_error);
                        }
                    }
                    eventsRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (isAdded()) {
                        showSnackBar(R.string.snackbar_error);
                    }
                    eventsRefreshLayout.setRefreshing(false);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.toString());
                if (isAdded()) {
                    showSnackBarWithWirelessSetting(R.string.snackbar_cannot_reach_servers);
                }
                eventsRefreshLayout.setRefreshing(false);
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


    private final BroadcastReceiver handleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        eventsAdapter.changeCursor(dbMethods.queryEvents(null, null,
                null, DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 0));
        eventsAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Shows Snackbar without any action button.
     *
     * @param stringResource Resource id for string to be shown on snackbar
     */
    private void showSnackBar(final int stringResource) {
        Snackbar.make(getActivity().findViewById(R.id.container),
                getResources().getString(stringResource), Snackbar.LENGTH_LONG).show();
    }

    /**
     * Shows Snackbar without any action button.
     *
     * @param string   String to be shown on snackbar
     */
    private void showSnackBar(final String string) {
        Snackbar.make(getActivity().findViewById(R.id.container),
                string, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Shows Snackbar with Action button for wireless settings.
     *
     * @param stringResource Resource id for string to be shown on snackbar
     */
    private void showSnackBarWithWirelessSetting(final int stringResource) {
        Snackbar.make(getActivity().findViewById(R.id.container),
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

    @Override
    public void onDestroy() {
        try {
            getActivity().getApplicationContext().unregisterReceiver(handleMessageReceiver);

        } catch (Exception e) {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }
}
