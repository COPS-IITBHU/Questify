package in.shriyansh.streamify.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.adapters.ViewPagerAdapter;
import in.shriyansh.streamify.customui.SlidingTabLayout;
import in.shriyansh.streamify.database.DbMethods;
import in.shriyansh.streamify.fcm.FcmMessagingService;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;
import in.shriyansh.streamify.fragments.Dashboard;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import static in.shriyansh.streamify.utils.PreferenceUtils.PREF_USER_POST_HOLDER;


public class MainActivity extends AppCompatActivity implements Urls, Dashboard.OnFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;

    private final CharSequence[] titles = {"0","0","0"};
    private static final int POSITION_NEWS = 0;
    private static final int POSITION_EVENTS = 1;

    private DbMethods dbMethods;
    private RequestQueue volleyQueue;

//    TODO Search
//    private SearchView mSearchView;
//    private MenuItem searchMenuItem;
//    MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        setUpToolbar();
        setPostFab();

        volleyQueue = Volley.newRequestQueue(this);
        dbMethods = new DbMethods(this);

        fetchLatestData();
//
//        //TODO Search
//        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Log.d("shriyansh query1",query);
//                //Do some magic
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                //searchView.setSuggestions();
//                Log.d("shriyansh query",newText);
//                String suggestions[];
//                Cursor searchCursor = dbMethods.queryStreams(null,
//                    DbContract.Streams.COLUMN_TITLE + " LIKE ? ",
//                    new String[] {"%"+newText+"%"},null,0);
//                suggestions = new String[searchCursor.getCount()];
//                int counter = 0;
//                if(searchCursor.moveToFirst()){
//                    do{
//                        suggestions[counter++] = searchCursor.getString(
//                            searchCursor.getColumnIndex(DbContract.Streams.COLUMN_TITLE));
//                    }while (searchCursor.moveToNext());
//                }
//                //searchCursor.close();
//                //searchView.setSuggestions(suggestions);
//                if(suggestions.length>0){
//                    Log.d("Shriyansh Suggestions",suggestions[0]);
//                }
//
//                searchView.setAdapter(new SearchCursorAdapter(getApplicationContext(),
//                    searchCursor));
//                searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position,
//                                            long id) {
//                        Log.d("ShriyanshTAG",view.getTag()+"");
//                    }
//                });
//
//                return false;
//            }
//        });
//            TODO Search
//        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
//            @Override
//            public void onSearchViewShown() {
//                //Do some magic
//                Log.d("shriyansh query2","hi");
//            }
//
//            @Override
//            public void onSearchViewClosed() {
//                //Do some magic
//                Log.d("shriyansh query3","hi");
//
//            }
//        });
//        TODO Search
//        searchView.setTextColor(getResources().getColor(R.color.ColorPrimary));
//        searchView.setHintTextColor(getResources().getColor(R.color.text_color_light));
    }

    /**
     * Call server to fetch all latest data of Notification, Events and Streams.
     */
    private void fetchLatestData() {
//        getStreams(PreferenceUtils.getStringPreference(MainActivity.this,
//                PreferenceUtils.PREF_USER_GLOBAL_ID));
//        getNotifications(PreferenceUtils.getStringPreference(MainActivity.this,
//                PreferenceUtils.PREF_USER_GLOBAL_ID),
//                dbMethods.queryLastNotificationId() + "");
//        getEvents(PreferenceUtils.getStringPreference(MainActivity.this,
//                PreferenceUtils.PREF_USER_GLOBAL_ID),dbMethods.queryLastEventId() + "");


    }

    /**
     *  Initializes Ui elements on the view.
     */
    private void initUi() {
        toolbar = findViewById(R.id.tool_bar);
        fab = findViewById(R.id.fab);
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);

        // Info: Hide FAB if user's not a position holder
        boolean isPostHolder = PreferenceUtils.getBooleanPreference(MainActivity.this,PREF_USER_POST_HOLDER);
//        if (!isPostHolder)
//            fab.setVisibility(View.GONE);

        setSupportActionBar(toolbar);
        //TODO Search
        //searchView = (MaterialSearchView) findViewById(R.id.search_view);
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles);
        pager.setAdapter(adapter);
        // Assigning the Sliding Tab Layout View
        tabs.setCustomTabView(R.layout.custom_tab_item, R.id.title_text);
        // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        tabs.setDistributeEvenly(true);
        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
    }

    /**
     * Sets navigation to Post activity.
     */
    private void setPostFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ChooseNotifType.class);
                startActivity(intent);
            }
        });
    }

    /**
     *  Shows new counter for each tab.
     *
     * @param position  Tab position
     * @param count     Count to be shown on Tabs
     */
    private void showNewItem(int position, String count) {
        adapter.setTitles(count,position);
        adapter.notifyDataSetChanged();
        if (pager != null) {
            pager.setAdapter(adapter);
            tabs.setViewPager(pager);
            Intent intent = getIntent();
            if (intent.hasExtra(FcmMessagingService.EXTRA_NOTIFICATION_TYPE_KEY)) {
                int notificationType = intent.getIntExtra(
                        FcmMessagingService.EXTRA_NOTIFICATION_TYPE_KEY,-1);
                if (notificationType == FcmMessagingService.NOTIFICATION_TYPE_NEWS) {
                    pager.setCurrentItem(POSITION_NEWS);
                } else if (notificationType == FcmMessagingService.NOTIFICATION_TYPE_EVENT) {
                    pager.setCurrentItem(POSITION_EVENTS);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_streams) {
            Intent intent = new Intent(MainActivity.this,StreamSubscribeActivity.class);
            startActivityForResult(intent, 6);
            return true;
        }
          //TODO search
//        if (id == R.id.action_search) {
//            Intent intent = new Intent(MainActivity.this,SearchResultActivity.class);
//            startActivityForResult(intent, 6);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetches new notifications from the server using volley.
     *
     * @param userId User'd global Id
     * @param lastNotificationId id of the last notification received
     */
    private void getNotifications(String userId,String lastNotificationId) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.NOTIFICATION_PARAM_USER_ID,userId);
        params.put(Constants.NOTIFICATION_PARAM_LAST_NOTIFICATION_ID,lastNotificationId);
        Log.d(TAG,params.toString());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                GET_NOTIFICATIONS, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                Log.d(TAG, resp.toString());
                try {
                    String status = resp.getString(Constants.RESPONSE_STATUS_KEY);
                    if (status.equals("200")) {
                        long count = dbMethods.insertNotifications(resp.getJSONObject("data")
                                .getJSONArray("notifications"));
                        showNewItem(POSITION_NEWS,String.valueOf(count));
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
     * Fetches new Events from the server using volley.
     *
     * @param userId User's global Id
     * @param lastEventId id of the last event received
     */
    private void getEvents(String userId,String lastEventId) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.EVENT_PARAM_USER_ID,userId);
        params.put(Constants.EVENT_PARAM_LAST_EVENT_ID,lastEventId);
        Log.d(TAG,params.toString());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                GET_EVENTS, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject resp) {
                Log.d(TAG, resp.toString());
                try {
                    String status = resp.getString(Constants.RESPONSE_STATUS_KEY);
                    if (status.equals(Constants.RESPONSE_STATUS_VALUE_OK)) {
                        long count = dbMethods.insertEvents(resp.getJSONObject("data")
                                .getJSONArray("events"));
                        showNewItem(POSITION_EVENTS,String.valueOf(count));
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

    @Override
    public void onBackPressed() {

        if (pager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            pager.setCurrentItem(0);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
