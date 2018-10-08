package in.shriyansh.streamify.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.activities.ChooseEvent;
import in.shriyansh.streamify.activities.MainActivity;
import in.shriyansh.streamify.activities.RegisterTeam;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;

import static android.graphics.BitmapFactory.*;
import static android.support.v7.widget.RecyclerView.*;
import static in.shriyansh.streamify.network.Urls.GET_PAST_TEAMS;
import static in.shriyansh.streamify.network.Urls.LIST_ALL_TEAMS;
import static in.shriyansh.streamify.utils.PreferenceUtils.PREF_USER_ROLL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Dashboard.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Dashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Dashboard extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView roll;
    private TextView year;
    private TextView branch;
    private TextView contact;
    private TextView name;
    private TextView email;
    private ImageView profilepic;
    private Button register_btn;
    private RecyclerView team_recycler;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String[] team_name, team_id;

    private OnFragmentInteractionListener mListener;

    public Dashboard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Dashboard.
     */
    // TODO: Rename and change types and number of parameters
    public static Dashboard newInstance(String param1, String param2) {
        Dashboard fragment = new Dashboard();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        roll = view.findViewById(R.id.dashboard_roll);
        year = view.findViewById(R.id.dashboard_year);
        contact = view.findViewById(R.id.dashboard_contact);
        email = view.findViewById(R.id.dashboard_email);
        branch = view.findViewById(R.id.dashboard_branch);
        name = view.findViewById(R.id.dashboard_name);
        profilepic = view.findViewById(R.id.pofilepic);
        register_btn = view.findViewById(R.id.reg_btn);
        team_recycler = view.findViewById(R.id.teamrecycler);

        roll.setText(PreferenceUtils.getStringPreference(getActivity(), PreferenceUtils.PREF_USER_ROLL));
        name.setText(PreferenceUtils.getStringPreference(getActivity(), PreferenceUtils.PREF_USER_NAME));
        email.setText(PreferenceUtils.getStringPreference(getActivity(), PreferenceUtils.PREF_USER_EMAIL));
        year.setText(PreferenceUtils.getStringPreference(getActivity(), PreferenceUtils.PREF_USER_YEAR_JOIN));
        branch.setText(PreferenceUtils.getStringPreference(getActivity(), PreferenceUtils.PREF_USER_BRANCH));
        contact.setText(PreferenceUtils.getStringPreference(getActivity(), PreferenceUtils.PREF_USER_CONTACT));
        setProfilePic(profilepic);

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseEvent.class);

                getActivity().startActivity(intent);
            }
        });

        return view;
    }

    void refreshTeamList(){
        Map<String,String> params = new HashMap<>();
        params.put("email", PreferenceUtils.getStringPreference(getActivity(), PreferenceUtils.PREF_USER_EMAIL));

        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST,
                GET_PAST_TEAMS, new JSONObject(params), new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                try {
                    if (response.getString("status").equals("200")) {
                        team_name = new String[response.getJSONArray("response").length()];
                        team_id = new String[response.getJSONArray("response").length()];
                        Log.e(TAG, "***************************");
                        Log.e(TAG, response.toString());
                        Log.e(TAG, "***************************");
                        Log.e(TAG, Integer.toString(response.length()));
                        Log.e(TAG, "***************************");

                        if (response.length() != 0) {
                            for (int i = 0; i < response.length(); i++) {

                                try {
//                            team_name[i] = response.getJSONObject(i).getString("team_name");
                                    team_id[i] = response.getJSONArray("response").getJSONObject(i).getString("teamid");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), "No teams", Toast.LENGTH_LONG).show();
                        }

                        /*****************************/

                        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                        team_recycler.setLayoutManager(llm);

                        RVAdapter adapter = new RVAdapter(team_id);             //change arguments here if API return changes
                        team_recycler.setAdapter(adapter);

                        /*********************************/

                    }

                    else if (response.getString("response").equals("No teams for given email ID exists")) {
                        Toast.makeText(getActivity(), "You are not registered in any teams", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "WHOOPS!! Something went wrong (ResponseError)", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }
        );

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.HTTP_INITIAL_TIME_OUT,
                Constants.HTTP_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        requestQueue.add(jsonArrayRequest);
    }


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{

//        private String[] team_set;
        private String[] team_id_array;

        public class PersonViewHolder extends ViewHolder {
            CardView cv;
//            TextView teamName;
            TextView teamId;
//            ImageView eventPhoto;

            PersonViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.team_card);
//                teamName = itemView.findViewById(R.id.team_name);
                teamId = itemView.findViewById(R.id.team_id);
//                eventPhoto = itemView.findViewById(R.id.eventLogo);
            }
        }

        private RVAdapter (String[] team_id) {        //change arguments here if API return changes
//            team_set = team_array;
            team_id_array = team_id;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.teams_card_view, parent, false);
            PersonViewHolder vh = new PersonViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(PersonViewHolder holder, int position) {
//            holder.teamName.setText(team_set[position]);
            holder.teamId.setText(team_id_array[position]);
//            holder.eventPhoto.setImageResource(R.drawable.ic_logo);
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }

    public static final String TAG = "SampleActivity";

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setProfilePic(ImageView profilePic) {
        try {
            File f=new File(getActivity().getApplicationContext().getFilesDir().getPath(), "profile.jpg");
            Bitmap b = decodeStream(new FileInputStream(f));
            profilePic.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
