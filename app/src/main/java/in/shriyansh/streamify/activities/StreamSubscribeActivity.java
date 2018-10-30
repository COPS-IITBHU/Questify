package in.shriyansh.streamify.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.utils.PreferenceUtils;

public class StreamSubscribeActivity extends AppCompatActivity {

    private String TAG = "SubscribeStreamActivity";
    private String allStreams[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_subscribe);

        // init UI
        RecyclerView mRecyclerView = findViewById(R.id.RecyclerViewSubscribeStream);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        allStreams = new String[]{"AERO", "ASTRO", "BIZ", "COPS", "CSI", "ROBO", "SAE"};
        RecyclerView.Adapter mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
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

            final String stream = allStreams[position];
            holder.mTextView.setText(stream);
            final boolean alreadyChecked = PreferenceUtils.getBooleanPreference(
                    StreamSubscribeActivity.this, PreferenceUtils.PREF_STREAMS.get(stream));
            holder.mSwitch.setChecked(alreadyChecked);

            holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !alreadyChecked)
                        PreferenceUtils.setBooleanPreference(
                        StreamSubscribeActivity.this, PreferenceUtils.PREF_STREAMS.get(stream), true);
                    else if (!isChecked && alreadyChecked)
                        PreferenceUtils.setBooleanPreference(
                                StreamSubscribeActivity.this, PreferenceUtils.PREF_STREAMS.get(stream), false);

                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return allStreams.length;
        }
    }

    // when user hits back or app stops
    @Override
    protected void onPause(){
        super.onPause();

        FirebaseMessaging instance = FirebaseMessaging.getInstance();
        for(final String stream: allStreams)
            if (PreferenceUtils.getBooleanPreference(
                StreamSubscribeActivity.this, PreferenceUtils.PREF_STREAMS.get(stream)))
                instance.subscribeToTopic(stream).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Subscribed to topic: "+stream);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Failed subscribing to topic: "+stream);
                    }
                });
            else
                instance.unsubscribeFromTopic(stream).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Unsubscribed to topic: "+stream);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Failed unsubscribing to topic: "+stream);
                    }
                });

    }
}
