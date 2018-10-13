package in.shriyansh.streamify.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.ui.LabelledSpinner;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;

import static in.shriyansh.streamify.network.Urls.LOGIN_URL;

public class LogInActivity extends AppCompatActivity {

    private LabelledSpinner email_spinner;
    private Button btn_login;
    private String email;
    private List<String> emails;
    private RequestQueue requestQueue;
    private ConstraintLayout loginLayout;
    private LinearLayout loginLayoutProcess;
    private LinearLayout progressLogin;
    private final static String TAG = "LoginActivity";

    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 10;
    private static final int CASE_REGISTER = 1;
    private static final int CASE_ERROR = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        requestQueue = Volley.newRequestQueue(LogInActivity.this);

        initUI();

        if (isAccountPermissionAvailable()) {
            emails = getEmails();
            addEmailsToAutoComplete(emails);
        }

        setListeners();

    }

    private List getEmails() {
        final List<String> emails = new ArrayList<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(LogInActivity.this).getAccounts();
        for (Account account : accounts)
            if (emailPattern.matcher(account.name).matches())
                emails.add(account.name);

        return emails;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LogInActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        email_spinner.setCustomAdapter(adapter);
    }


    private void setListeners() {

        email_spinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView,
                                     View itemView, int position, long id) {
                email = emails.get(position);
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {
                email = emails.get(0);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressLogin.setVisibility(View.VISIBLE);
                loginLayoutProcess.setVisibility(View.GONE);

                View focusView = null;
                boolean cancel = false;

                if (!email.contains("itbhu")){
                    showSnackBar("Use your Institute Email ID!!","RETRY",CASE_ERROR);
                    focusView = email_spinner;
                    cancel = true;
                }
                if (cancel) {
                    progressLogin.setVisibility(View.GONE);
                    loginLayoutProcess.setVisibility(View.VISIBLE);
                    focusView.requestFocus();
                }
                else {

                    login();
                    getJoiningYear();
                }
            }
        });

    }

    private void getJoiningYear() {

        if (email.contains(".bce")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Biochemical Engineering");
        }
        else if (email.contains(".bme")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Biomedical Engineering");
        }
        else if (email.contains(".app") || email.contains(".phy")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Engineering Physics");
        }
        else if (email.contains(".mec")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Mechanical Engineering");
        }
        else if (email.contains(".apc")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Industrial Chemistry");
        }
        else if (email.contains(".mat") || email.contains(".apm")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Mathematics and Computing");
        }
        else if (email.contains(".che")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Chemical Engineering");
        }
        else if (email.contains(".cer")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Ceramics Engineering");
        }
        else if (email.contains(".min")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Mining Engineering");
        }
        else if (email.contains(".met")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Metallurgical Engineering");
        }
        else if (email.contains(".mst")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Material Science and Technology");
        }
        else if (email.contains(".phe")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Pharmaceutical Engineering");
        }
        else if (email.contains(".eee")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Electrical Engineering");
        }
        else if (email.contains(".cse")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Computer Science and Engineering");
        }
        else if (email.contains(".ece")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Electronics Engineering");
        }
        else if (email.contains(".civ")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Civil Engineering");
        }
        else if (email.contains(".hss")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Humanistic Studies");
        }



        if (email.contains("18")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2018");
        }
        else if (email.contains("17")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2017");
        }
        else if (email.contains("16")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2016");
        }
        else if (email.contains("15")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2015");
        }
        else if (email.contains("14")) {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2014");
        }
        else {
            PreferenceUtils.setStringPreference(LogInActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "Earlier than 2014");
        }

    }

    private void login() {

        Map<String, String> params = new HashMap<>();
        params.put("email", email);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                LOGIN_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try {
                    String status = response.getString("status");
                    if (status.equals("200")) {
                        PreferenceUtils.setStringPreference(LogInActivity.this,
                                PreferenceUtils.PREF_USER_CONTACT,
                                response.getJSONObject("response").getString("contact"));
                        PreferenceUtils.setStringPreference(LogInActivity.this,
                                PreferenceUtils.PREF_USER_EMAIL,
                                response.getJSONObject("response").getString("email"));
                        PreferenceUtils.setStringPreference(LogInActivity.this,
                                PreferenceUtils.PREF_USER_FCM_TOKEN,
                                response.getJSONObject("response").getString("fcmtoken"));
                        PreferenceUtils.setStringPreference(LogInActivity.this,
                                PreferenceUtils.PREF_USER_NAME,
                                response.getJSONObject("response").getString("name"));
                        PreferenceUtils.setStringPreference(LogInActivity.this,
                                PreferenceUtils.PREF_USER_ROLL,
                                response.getJSONObject("response").getString("rollno"));
                        PreferenceUtils.setBooleanPreference(LogInActivity.this,
                                PreferenceUtils.PREF_USER_POST_HOLDER,
                                response.getJSONObject("response").getBoolean("isPositionHolder"));
                        PreferenceUtils.setBooleanPreference(LogInActivity.this,
                                PreferenceUtils.PREF_USER_LOGGED_IN,
                                true);

                        if (PreferenceUtils.getBooleanPreference(LogInActivity.this, PreferenceUtils.PREF_USER_LOGGED_IN)) {
                            Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    progressLogin.setVisibility(View.GONE);
                    loginLayoutProcess.setVisibility(View.VISIBLE);
                }
            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.toString());
                showSnackBar("Network Unreachable!","RETRY",CASE_REGISTER);

                progressLogin.setVisibility(View.GONE);
                loginLayoutProcess.setVisibility(View.VISIBLE);
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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.HTTP_INITIAL_TIME_OUT,
                Constants.HTTP_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);

    }

    private void showSnackBar(String msg, String action, final int caseId) {
        Snackbar snackbar = Snackbar
                .make(loginLayout, msg, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (caseId == CASE_REGISTER) {
                            login();
                        }
                    }
                });
        snackbar.setActionTextColor(getResources().getColor(R.color.pink500));
        snackbar.show();
    }



    private void initUI() {

        email_spinner = findViewById(R.id.et_email_login);
        btn_login = findViewById(R.id.btn_login_submit);
        loginLayout = findViewById(R.id.login_layout);
        loginLayoutProcess = findViewById(R.id.login_process);
        progressLogin = findViewById(R.id.progress_layout_login);

        PreferenceUtils.setBooleanPreference(LogInActivity.this, PreferenceUtils.PREF_USER_LOGGED_IN, false);

    }

    private boolean isAccountPermissionAvailable() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.GET_ACCOUNTS)) {
                createDialog("","Email is required to authenticate",
                        "Try Again","Deny",false,null,
                        true);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            }
        } else {
            Log.d("LoginActivity","Accounts permission Available");
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    emails = getEmails();
                    addEmailsToAutoComplete(emails);
                } else {
                    isAccountPermissionAvailable();
                }
            }
            break;
            default:
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("updateRecyclerView",true);
        boolean shouldRefresh = bundle.getBoolean("updateRecyclerView");
        if (shouldRefresh) {
            //Refresh your recyclerView
        }
    }

    private void createDialog(String title, String msg, String positiveBtn, String negativeBtn,
                              boolean cancellable, final Intent intent, boolean finishActivity) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LogInActivity.this);
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
        if (finishActivity) {
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


}
