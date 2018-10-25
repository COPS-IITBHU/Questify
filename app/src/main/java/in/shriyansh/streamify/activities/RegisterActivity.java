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
import android.support.annotation.NonNull;
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

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.ui.LabelledSpinner;
import in.shriyansh.streamify.utils.PreferenceUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class RegisterActivity extends Activity implements Urls {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private EditText etName;
    private EditText etContact;
    private EditText rollNo;
    private Button btnRegister;
    private LabelledSpinner etEmail;
    private LinearLayout loginLayout;
    private LinearLayout progressLayout;
    private CoordinatorLayout coordinatorLayout;
    private Button btn_log_in;

    public List<String> emails;
    private String email;

    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 10;
    private static final int CASE_REGISTER = 1;
    private static final int CASE_ERROR = 0;

    private static final int PHONE_NUMBER_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        initUi();

        if (PreferenceUtils.getBooleanPreference(this,PreferenceUtils.PREF_USER_LOGGED_IN)) {
            if (PreferenceUtils.getBooleanPreference(RegisterActivity.this,
                    PreferenceUtils.PREF_IS_FCM_REGISTERED)) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // update FCM Token Whenever on every login
                String fcmToken = PreferenceUtils.getStringPreference(RegisterActivity.this,
                        PreferenceUtils.PREF_FCM_TOKEN);
                if (!fcmToken.contentEquals(""))
                    //fcm token available
                    registerFcmToken(fcmToken);

                // no fcm token found register on next opening //or on main activity show user
                // snack bar to update fcm token
                // or create a broadcast to register fcm token in mainActivity
            }
        }

        if (isAccountPermissionAvailable()) {
            emails = getEmails();
            addEmailsToAutoComplete(emails);
        }
    }

    private void initUi() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etContact = findViewById(R.id.et_contact);
        rollNo = findViewById(R.id.roll_number);
        btnRegister = findViewById(R.id.btn_register);
        loginLayout = findViewById(R.id.layout_register);
        progressLayout = findViewById(R.id.layout_progress);
        btn_log_in = findViewById(R.id.btn_sign_in);

        if (!PreferenceUtils.getStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_NAME).equals("pref_user_name"))
            etName.setText(PreferenceUtils.getStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_NAME));

        if (!PreferenceUtils.getStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_ROLL).equals("pref_user_roll"))
            rollNo.setText(PreferenceUtils.getStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_ROLL));

        if (!PreferenceUtils.getStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_CONTACT).equals("pref_user_contact"))
            etContact.setText(PreferenceUtils.getStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_CONTACT));

        setListeners();
    }

    private void setListeners() {
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
                /*
                storeUserInformationAndProceed("444", "yashhg", "nksnfkjns",
                "1234567890", "uhcbjhb7zdckhDC87", "17135096");
                 */

                attemptLogin();
            }
        });

        etEmail.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
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

        btn_log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Fetches emails from user's account.
     *
     * @return List of emails
     */
    public List<String> getEmails() {
        final List<String> emails = new ArrayList<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(RegisterActivity.this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches())
                emails.add(account.name);
        }
        return emails;
    }

    /**
     * Adds email to autocomplete textView.
     *
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
     * Checks if Account Permission is granted.
     *
     * @return True if granted
     */
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
            Log.d(TAG,"Accounts permission Available");
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

    /**
     *  Attempts Login for user.
     */
    private void attemptLogin() {
        boolean cancel = false;
        String name;
        String contact;
        String rollno;

        // Reset errors.
        etName.setError(null);
        etContact.setError(null);
        View focusView = null;

        // Store values at the time of the login attempt.
        name = etName.getText().toString();
        contact = etContact.getText().toString();
        rollno = rollNo.getText().toString();


        if (!email.contains("itbhu")){
            showSnackBar("Use your Institute Email ID!!","RETRY",CASE_ERROR);
            focusView = etEmail;
            cancel = true;
        }

        if (name.length() == 0) {
            etName.setError(Html.fromHtml(
                    "<font color='#ffffff'>Name cannot be empty !</font>"));
            focusView = etName;
            cancel = true;
        }

        if (rollno.length() < 8) {
            rollNo.setError(Html.fromHtml(
                    "<font color='#ffffff'>Roll Number cannot be less than 8 digits!</font>"));
            focusView = rollNo;
            cancel = true;
        }

        if (contact.length() < PHONE_NUMBER_LENGTH) {
            etContact.setError(Html.fromHtml(
                    "<font color='#ffffff'>Please enter your 10 digit phone number</font>"));
            focusView = etContact;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);

            storeUserInformationAndProceed(name, email, contact, rollno);

        }
    }

    private void registerFcmToken(String token) {
        Log.d(TAG, "Updating FCM token: " + token);

        // Save updated token
        PreferenceUtils.setStringPreference(this,PreferenceUtils.PREF_FCM_TOKEN, token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\n\t\"email\":\"" +
                        PreferenceUtils.getStringPreference(this,PreferenceUtils.PREF_USER_EMAIL) +
                        "\",\n\t\"fcmToken\":\"" + token +"\"\n}");
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Urls.FCM_UPDATE)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try{
            okhttp3.Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                Log.e(TAG, "onNewToken failed:" + response.body().string());
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Stores server confirmed user credentials.
     *
     * @param userName      User's name
     * @param userEmail     User's email
     * @param userContact   User's contact
     */
    private void storeUserInformationAndProceed(final String userName,
                                                final String userEmail, final String userContact,
                                                final String rollNo) {
        PreferenceUtils.setStringPreference(RegisterActivity.this,
                PreferenceUtils.PREF_USER_NAME, userName);
        PreferenceUtils.setStringPreference(RegisterActivity.this,
                PreferenceUtils.PREF_USER_EMAIL, userEmail);
        PreferenceUtils.setStringPreference(RegisterActivity.this,
                PreferenceUtils.PREF_USER_CONTACT, userContact);
        PreferenceUtils.setStringPreference(RegisterActivity.this,
                PreferenceUtils.PREF_USER_ROLL, rollNo);

        if (email.contains(".bce")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Biochemical Engineering");
        }
        else if (email.contains(".bme")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Biomedical Engineering");
        }
        else if (email.contains(".app") || email.contains(".phy")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Engineering Physics");
        }
        else if (email.contains(".mec")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Mechanical Engineering");
        }
        else if (email.contains(".apc")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Industrial Chemistry");
        }
        else if (email.contains(".mat") || email.contains(".apm")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Mathematics and Computing");
        }
        else if (email.contains(".che")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Chemical Engineering");
        }
        else if (email.contains(".cer")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Ceramics Engineering");
        }
        else if (email.contains(".min")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Mining Engineering");
        }
        else if (email.contains(".met")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Metallurgical Engineering");
        }
        else if (email.contains(".mst")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Material Science and Technology");
        }
        else if (email.contains(".phe")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Pharmaceutical Engineering");
        }
        else if (email.contains(".eee")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Electrical Engineering");
        }
        else if (email.contains(".cse")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Computer Science and Engineering");
        }
        else if (email.contains(".ece")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Electronics Engineering");
        }
        else if (email.contains(".civ")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Civil Engineering");
        }
        else if (email.contains(".hss")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_BRANCH, "Humanistic Studies");
        }



        if (email.contains("18")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2018");
        }
        else if (email.contains("17")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2017");
        }
        else if (email.contains("16")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2016");
        }
        else if (email.contains("15")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2015");
        }
        else if (email.contains("14")) {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "2014");
        }
        else {
            PreferenceUtils.setStringPreference(RegisterActivity.this, PreferenceUtils.PREF_USER_YEAR_JOIN, "Earlier than 2014");
        }


        Intent intent = new Intent(
                RegisterActivity.this, GetOptionalProfilePic.class);
        startActivity(intent);
        finish();
    }

    /**
     * Creates dialog for account permission rationale.
     *
     * @param title             Dialog title
     * @param msg               Dialog message
     * @param positiveBtn       Dialog positive button text
     * @param negativeBtn       Dialog negative button text
     * @param cancellable       Is Dialog cancellable
     * @param intent            Intent to fire on positive button click
     * @param finishActivity    Whether to finish the activity or not on negative response
     */
    private void createDialog(String title, String msg, String positiveBtn, String negativeBtn,
                              boolean cancellable, final Intent intent, boolean finishActivity) {
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

    private void showSnackBar(String msg, String action, final int caseId) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (caseId == CASE_REGISTER) {
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
