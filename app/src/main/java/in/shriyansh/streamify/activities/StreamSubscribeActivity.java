package in.shriyansh.streamify.activities;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.PreferenceUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StreamSubscribeActivity extends AppCompatActivity {

    private String TAG = "SubscribeStreamActivity";
    private RecyclerView.Adapter mAdapter;
    private ProgressBar progressBar;
    private List<String> allStreams;
    private List<String> myStreams;
    private String email;  // user's email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_subscribe);

        // Get user's email
        email = PreferenceUtils.getStringPreference(getApplicationContext(),
                PreferenceUtils.PREF_USER_EMAIL);


        // init UI
        RecyclerView mRecyclerView = findViewById(R.id.RecyclerViewSubscribeStream);
        progressBar = findViewById(R.id.ProgressBarSubscribeStream);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        allStreams = new ArrayList<>();
        myStreams = new ArrayList<>();
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);

        progressBar.setVisibility(View.VISIBLE);

        fetchStreamDetails();
    }

    private void fetchStreamDetails() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Urls.LIST_ALL_STREAMS)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // on failure response
                Snackbar.make(findViewById(R.id.SubscribeStreamActivityMainLayout),
                        "onFailure: "+e.toString(),Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // get stream titles
                // subscribe to them when user hits save

                try {
                    String jsonData = response.body().string();
                    JSONObject object = new JSONObject(jsonData);

                    String status = object.getString("status");
                    if (status.equals("200")){
                        // get stream titles
                        JSONArray array = object.getJSONArray("response");

                        // create a list
                        for(int i=0;i<array.length(); ++i)
                            allStreams.add(array.getJSONObject(i).getString("title"));

                        fetchSubscribedStreamDetails();
                    }else{
                        throw new Exception("onResponse Status: " + status);
                    }
                }catch (Exception e){
                    // on failure response
                    Snackbar.make(findViewById(R.id.SubscribeStreamActivityMainLayout),
                            "Exception: "+e.toString(),Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * get which streams have i subscribed to
     */
    private void fetchSubscribedStreamDetails() {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"email\":\"" + email + "\"\n}");

        Request request = new Request.Builder()
                .url(Urls.GET_STREAMS)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // on failure response
                Snackbar.make(findViewById(R.id.SubscribeStreamActivityMainLayout),
                        "onFailure: "+e.toString(),Snackbar.LENGTH_LONG).show();

                // set the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        mAdapter.notifyDataSetChanged();
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
                            myStreams.add(array.getJSONObject(i).getString("streamtitle"));
                    }
                    else
                        throw new Exception("onResponse Status: " + status);
                }catch (Exception e){
                    // on failure response
                    Snackbar.make(findViewById(R.id.SubscribeStreamActivityMainLayout),
                            "Exception: "+e.toString(),Snackbar.LENGTH_LONG).show();
                } finally{
                    // set the UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    // Upload user's stream choices to server
    // Return to MainActivity. Filter posts according to user streams in it
    // Recall that this activity is called using startActivityForResult
    private void savePreferences() {

    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            TextView mTextView;
            Switch mSwitch;

            MyViewHolder(View v) {
                super(v);
                mTextView = v.findViewById(R.id.TextStreamSubscribeItem);
                mSwitch = v.findViewById(R.id.SwitchStreamSubscribeItem);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        MyAdapter() {}

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_item_stream_subscribe, parent, false);

            return new MyViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            final String stream = allStreams.get(position);
            holder.mTextView.setText(stream);
            holder.mSwitch.setChecked(myStreams.contains(stream));

            holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !myStreams.contains(stream))
                        myStreams.add(stream);
                    else if ( !isChecked  && myStreams.contains(stream))
                        myStreams.remove(stream);
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return allStreams.size();
        }
    }

    // when user hits back or app stops
    // TODO Maybe ye kafi overload dalega. Pinging server every frequently
    @Override
    protected void onPause(){
        super.onPause();

        // subscribe to myStreams
        for(String s: myStreams)
            subscribe(s);

        for(String s: allStreams)
            if (!myStreams.contains(s))
                unsubscribe(s);
    }

    void subscribe(String s){
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"email\":\"" + email +"\",\n\t\"stream\":\""+ s + "\"\n}");
        final Request request = new Request.Builder()
                .url(Urls.SUBSCRIBE_STREAM)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"subscribe response: "+response.body().string());
            }
        });
    }

    void unsubscribe(String s){
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"email\":\"" + email +"\",\n\t\"stream\":\""+ s + "\"\n}");
        Request request = new Request.Builder()
                .url(Urls.UN_SUBSCRIBE_STREAM)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"unsubscribe response: "+response.body().string());
            }
        });
    }
}
