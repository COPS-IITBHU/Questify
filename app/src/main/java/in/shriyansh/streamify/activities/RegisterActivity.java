package in.shriyansh.streamify.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.network.URLs;
import in.shriyansh.streamify.ui.LabelledSpinner;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;



public class RegisterActivity extends Activity implements URLs{
    public static final String TAG = RegisterActivity.class.getSimpleName();

    EditText etName,etContact;
    Button btnRegister;
    LabelledSpinner etEmail;
    LinearLayout loginLayout,progressLayout;
    CoordinatorLayout coordinatorLayout;

    List<String> emails;
    String email;

    final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 10;
    final int CASE_REGISTER = 1;

    RequestQueue volleyQueue;
    private static final int PHONE_NUMBER_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        volleyQueue = Volley.newRequestQueue(this);

        initUI();

        if(PreferenceUtils.getBooleanPreference(this,PreferenceUtils.PREF_IS_REGISTERED)){
            if(PreferenceUtils.getBooleanPreference(RegisterActivity.this,PreferenceUtils.PREF_IS_FCM_REGISTERED)){
                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                String fcmToken = PreferenceUtils.getStringPreference(RegisterActivity.this,PreferenceUtils.PREF_FCM_TOKEN);
                if(!fcmToken.contentEquals("")){
                    //fcm token available
                    String user_id = PreferenceUtils.getStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_GLOBAL_ID);
                    registerFcmToken(fcmToken,user_id);
                }
                //no fcm token found register on next opening //or on main activity show user snack bar to update fcm token
                //or create a broadcast to register fcm token in mainActivity

            }

        }

        if(isAccountPermissionAvailable()){
            emails=getEmails();
            addEmailsToAutoComplete(emails);
        }

        etContact.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.et_contact || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        etEmail.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                email = emails.get(position);
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {
                email = emails.get(0);
            }
        });
    }

    void initUI(){
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        etName=(EditText)findViewById(R.id.et_name);
        etEmail=(LabelledSpinner)findViewById(R.id.et_email);
        etContact=(EditText)findViewById(R.id.et_contact);
        btnRegister=(Button)findViewById(R.id.btn_register);
        loginLayout=(LinearLayout)findViewById(R.id.layout_register);
        progressLayout=(LinearLayout)findViewById(R.id.layout_progress);
    }

    /**
     * Fetches emails from user's account
     * @return List of emails
     */
    public List getEmails(){
        final List<String> emails = new ArrayList<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(RegisterActivity.this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name+"\n";
                emails.add(possibleEmail);
            }
        }
        return emails;
    }

    /**
     * Adds email to autocomplete textView
     * @param emailAddressCollection list of emails
     */
    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        etEmail.setCustomAdapter(adapter);
    }

    /**
     * Checks if Account Permission is granted
     * @return True if granted
     */
    public boolean isAccountPermissionAvailable(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.GET_ACCOUNTS)) {
                createDialog("","Email is required to authenticate","Try Again","Deny",false,null,true);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            }
        }else{
            Log.d(TAG,"Accounts permission Available");
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    emails=getEmails();
                    addEmailsToAutoComplete(emails);
                } else {
                    isAccountPermissionAvailable();
                }
            }
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("updateRecyclerView",true);
        boolean shouldRefresh = bundle.getBoolean("updateRecyclerView");
        if(shouldRefresh){
            //Refresh your recyclerView
        }
    }

    /**
     *
     */
    private void attemptLogin() {
        boolean cancel = false;
        String fcmToken,name,contact;

        // Reset errors.
        etName.setError(null);
        etContact.setError(null);
        View focusView = null;

        // Store values at the time of the login attempt.
        name = etName.getText().toString();
        contact =etContact.getText().toString();
        fcmToken = FirebaseInstanceId.getInstance().getToken();


        if(name.length()==0){
            etName.setError(Html.fromHtml("<font color='#ffffff'>Name cannot be empty !</font>"));
            focusView=etName;
            cancel=true;
        }

        if(contact.length()!=PHONE_NUMBER_LENGTH){
            etContact.setError(Html.fromHtml("<font color='#ffffff'>Please enter your 10 digit phone number</font>"));
            focusView = etContact;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);

            register(fcmToken,name,email,contact);
        }
    }

    public void registerFcmToken(String fcmToken,String user_id){
        Map<String, String> params = new HashMap<>();
        params.put("fcmToken", fcmToken);
        params.put("user_id",user_id);
        Log.d(TAG,params.toString());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                FCM_UPDATE, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                Log.d(TAG, resp.toString());
                try {
                    String status = resp.getString("status");
                    if (status.equals("OK")) {
                        JSONObject data = new JSONObject(resp.getString("data"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_GLOBAL_ID,data.getString("id"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_NAME,data.getString("name"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_EMAIL,data.getString("email"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_CONTACT,data.getString("contact"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_FCM_TOKEN,data.getString("fcmToken"));

                        PreferenceUtils.setBooleanPreference(RegisterActivity.this,PreferenceUtils.PREF_IS_REGISTERED,true);
                        if(!data.getString("fcmToken").contentEquals("")){
                            PreferenceUtils.setBooleanPreference(RegisterActivity.this,PreferenceUtils.PREF_IS_FCM_REGISTERED,true);
                        }
                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    showSnackBar("Error! Please Try Again!","RETRY",CASE_REGISTER);
                    loginLayout.setVisibility(View.VISIBLE);
                    progressLayout.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.toString());
                showSnackBar("Network Unreachable!","RETRY",CASE_REGISTER);
                loginLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
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

    public void register(String fcmToken, String name, String email, final String contact){

        Map<String, String> params = new HashMap<>();
        params.put("fcmToken", fcmToken);
        params.put("name",name);
        params.put("email",email);
        params.put("contact",contact);
        Log.d(TAG,params.toString());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                REGISTER_URL, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                Log.d(TAG, resp.toString());
                try {
                    String status = resp.getString("status");
                    if (status.equals("OK")) {
                        JSONObject data = new JSONObject(resp.getString("data"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_GLOBAL_ID,data.getString("id"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_NAME,data.getString("name"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_EMAIL,data.getString("email"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_CONTACT,data.getString("contact"));
                        PreferenceUtils.setStringPreference(RegisterActivity.this,PreferenceUtils.PREF_USER_FCM_TOKEN,data.getString("fcmToken"));

                        PreferenceUtils.setBooleanPreference(RegisterActivity.this,PreferenceUtils.PREF_IS_REGISTERED,true);
                        if(!data.getString("fcmToken").contentEquals("")){
                            PreferenceUtils.setBooleanPreference(RegisterActivity.this,PreferenceUtils.PREF_IS_FCM_REGISTERED,true);
                        }
                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    showSnackBar("Error! Please Try Again!","RETRY",CASE_REGISTER);
                    loginLayout.setVisibility(View.VISIBLE);
                    progressLayout.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.toString());
                showSnackBar("Network Unreachable!","RETRY",CASE_REGISTER);
                loginLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
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
     * Creates dialog for account permission rationale
     * @param title Dialog title
     * @param msg Dialog message
     * @param positiveBtn Dialog positive button text
     * @param negativeBtn Dialog negative button text
     * @param cancellable Is Dialog cancellable
     * @param intent Inetnt to fire on positive button click
     * @param finishActivity Whether to finish the activity or not on negative response
     */
    public void createDialog(String title,String msg,String positiveBtn, String negativeBtn,boolean cancellable,final Intent intent,boolean finishActivity){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(RegisterActivity.this);
        builder1.setTitle(title);
        builder1.setMessage(msg);
        builder1.setCancelable(cancellable);
        builder1.setPositiveButton(positiveBtn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       // handleConnectionAndProceed();

                    }
                });
        builder1.setNegativeButton(negativeBtn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(intent);
                    }
                });

        final AlertDialog alertDialog = builder1.create();
        if(finishActivity){
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    finish();
                    return false;
                }
            });
        }
        alertDialog.show();
    }

    public void showSnackBar(String msg, String action, final int caseId){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(caseId==CASE_REGISTER){
                            attemptLogin();
                        }
                    }
                });
        snackbar.setActionTextColor(getResources().getColor(R.color.pink500));
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
