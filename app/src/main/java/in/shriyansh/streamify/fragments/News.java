package in.shriyansh.streamify.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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
import in.shriyansh.streamify.activities.ImageLibrary;
import in.shriyansh.streamify.adapters.NotificationsAdapter;
import in.shriyansh.streamify.database.DbContract;
import in.shriyansh.streamify.database.DbMethods;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Fragment for News.
 */
public class News extends Fragment implements Urls {
    private static final String TAG = News.class.getSimpleName();
    public static final String NEWS_TITLE_KEY = "news_title_key";

    private SwipeRefreshLayout newsRefreshLayout;
    private ListView notificationsListView;

    private NotificationsAdapter notificationsAdapter;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO scroll on click of notification
        //notificationsListView.smoothScrollToPositionFromTop(4,0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_layout,container,false);

        notificationsListView = view.findViewById(R.id.notifications_list);
        notificationsAdapter = new NotificationsAdapter(getActivity(),dbMethods.queryNotifications(
                null, null, null,
                DbContract.Notifications.COLUMN_GLOBAL_ID + " DESC ", 0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationsListView.setNestedScrollingEnabled(true);
        }
        notificationsListView.setAdapter(notificationsAdapter);
        newsRefreshLayout = view.findViewById(R.id.news_refresh_layout);
        newsRefreshLayout.setColorSchemeResources(R.color.ColorPrimary, R.color.pink500,
                R.color.teal500);

        /*
         * Registering Receiver
         */
        IntentFilter filter = new IntentFilter(Constants.DISPLAY_MESSAGE_ACTION);
        getActivity().getApplicationContext().registerReceiver(handleMessageReceiver, filter);

        /*
         * Implementing refresh
         */
        newsRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNotifications(PreferenceUtils.getStringPreference(getActivity(),
                        PreferenceUtils.PREF_USER_GLOBAL_ID),
                        dbMethods.queryLastNotificationId() + "");
            }
        });
        notificationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (l != 1) {
                    Cursor cursor = dbMethods.queryNotifications(new String[]{
                            DbContract.Notifications.COLUMN_TYPE,
                            DbContract.Notifications.COLUMN_GLOBAL_ID,
                            DbContract.Notifications.COLUMN_TITLE},
                            DbContract.Notifications._ID + " = ?",
                            new String[]{l + ""},null,0);
                    int newsType = 0;
                    int newsGlobalId = 0;
                    String title = "";
                    while (cursor.moveToNext()) {
                        newsType = cursor.getInt(cursor.getColumnIndex(
                                DbContract.Notifications.COLUMN_TYPE));
                        newsGlobalId = cursor.getInt(cursor.getColumnIndex(
                                DbContract.Notifications.COLUMN_GLOBAL_ID));
                        title = cursor.getString(cursor.getColumnIndex(
                                DbContract.Notifications.COLUMN_TITLE));
                    }
                    if (newsType == DbContract.Notifications.VALUE_TYPE_IMAGE) {
                        Intent intent = new Intent(getActivity(), ImageLibrary.class);
                        intent.putExtra(NEWS_TITLE_KEY,title);
                        intent.putExtra(ImageLibrary.INTENT_KEY_NOTIFICATION_GLOBAL_ID,
                                newsGlobalId);
                        startActivityForResult(intent, 3);
                    } else if (newsType == DbContract.Notifications.VALUE_TYPE_VIDEO) {
                        // TODO Dont send video type news notifs
                    }
                }
            }
        });

        notificationsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        notificationsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

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
                        notificationsAdapter.deleteSelected();
                        actionMode.finish();
                        return true;
                    case R.id.action_share:
                        nr = 0;
                        notificationsAdapter.forwardSelected();
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                notificationsAdapter.clearSelection();
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long id,
                                                  boolean checked) {
                if (checked) {
                    nr++;
                    notificationsAdapter.setNewSelection(id,true);
                } else {
                    nr--;
                    notificationsAdapter.removeSelection(id);
                }
                actionMode.setTitle(nr + "");
            }
        });
        return view;
    }

    private final BroadcastReceiver handleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        notificationsAdapter.changeCursor(dbMethods.queryNotifications(null,null,
                null,
                DbContract.Notifications.COLUMN_GLOBAL_ID + " DESC ",0));
        notificationsAdapter.notifyDataSetChanged();
        }
    };

    private void getNotifications(String userId, String lastNotificationId) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.NOTIFICATION_PARAM_LAST_NOTIFICATION_ID,lastNotificationId);
        Log.d(TAG,params.toString());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                GET_NOTIFICATIONS, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                Log.d(TAG, resp.toString());
                try {
                    String status = resp.getString(Constants.RESPONSE_STATUS_KEY);
                    if (status.equals(Constants.RESPONSE_STATUS_VALUE_OK)) {
                        long count = dbMethods.insertNotifications(resp.getJSONObject("data")
                                .getJSONArray("notifications"));
                        notificationsAdapter.changeCursor(dbMethods.queryNotifications(
                                null, null, null,
                                DbContract.Notifications.COLUMN_GLOBAL_ID
                                        + " DESC ", 0));
                        notificationsAdapter.notifyDataSetChanged();

                        String notificationCount = count == 0 ? "No" : count + "";
                        if (isAdded()) {
                            showSnackBar(notificationCount + " new Notifications.");
                        }
                    } else {
                        if (isAdded()) {
                            showSnackBar(R.string.snackbar_error_fetching_notifications);
                        }
                    }
                    newsRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (isAdded()) {
                        showSnackBar(R.string.snackbar_error_fetching_notifications);
                    }
                    newsRefreshLayout.setRefreshing(false);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.toString());
                if (isAdded()) {
                    showSnackBarWithWirelessSetting(R.string.snackbar_cannot_reach_servers);
                }
                newsRefreshLayout.setRefreshing(false);
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
