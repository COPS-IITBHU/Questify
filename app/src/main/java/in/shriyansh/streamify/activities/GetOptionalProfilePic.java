package in.shriyansh.streamify.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;

import static in.shriyansh.streamify.network.Urls.REGISTER_URL;

public class GetOptionalProfilePic extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private Button btn_upload;
    private Button proceed;
    private Button btn_edit_info;
    private TextView user_name;
    private TextView user_roll;
    private TextView user_branch;
    private TextView user_contact;
    private TextView user_email;
    private TextView user_year_join;
    private ConstraintLayout coordinatorLayout;
    private LinearLayout picpage;
    private LinearLayout progresslayout;
    public static final int PICK_IMAGE = 1;

    private static final int CASE_REGISTER = 1;
    private static final int CASE_ERROR = 0;

    private String fcmToken;
    private String name;
    private String rollno;
    private String email;
    private String contact;

    private RequestQueue volleyQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_optional_profile_pic);

        btn_upload = findViewById(R.id.btn_pick_pic);
        proceed = findViewById(R.id.btn_set_pic);
        btn_edit_info = findViewById(R.id.edit_info);

        initUI();

        volleyQueue = Volley.newRequestQueue(this);


        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                picpage.setVisibility(View.GONE);
                progresslayout.setVisibility(View.VISIBLE);

                /***********************/
                // comment this if server is up

//                Intent intent = new Intent(GetOptionalProfilePic.this, MainActivity.class);
//                startActivity(intent);
//                finish();

                /************************/
                register(name,rollno, email,contact);     //uncomment this when server is up
                }
        });

        btn_edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetOptionalProfilePic.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        }

    private void initUI() {
        user_name = findViewById(R.id.user_name_disp);
        user_roll = findViewById(R.id.user_roll_disp);
        user_branch = findViewById(R.id.user_branch_disp);
        user_contact = findViewById(R.id.user_contact_disp);
        user_email = findViewById(R.id.user_email_disp);
        user_year_join = findViewById(R.id.user_year_join_disp);
        coordinatorLayout = findViewById(R.id.set_pic_layout);
        picpage = findViewById(R.id.pic_page);
        progresslayout = findViewById(R.id.progress_layout);

        name = PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_NAME);
        rollno = PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_ROLL);
        email = PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_EMAIL);
        contact = PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_CONTACT);

        user_name.setText(PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_NAME));
        user_roll.setText(PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_ROLL));
        user_year_join.setText(PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_YEAR_JOIN));
        user_email.setText(PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_EMAIL));
        user_branch.setText(PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_BRANCH));
        user_contact.setText(PreferenceUtils.getStringPreference(GetOptionalProfilePic.this, PreferenceUtils.PREF_USER_CONTACT));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        ImageView user_img = findViewById(R.id.user_profile_pic);

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    user_img.setImageBitmap(selectedImage);


                    File mypath=new File(getApplicationContext().getFilesDir().getPath(),"profile.jpg");

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(mypath);
                        // Use the compress method on the BitMap object to write image to the OutputStream
                        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //return directory.getAbsolutePath();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(GetOptionalProfilePic.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }


            }else {
                Toast.makeText(GetOptionalProfilePic.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void register(String rollNo, String name, String email, final String contact) {

        Map<String, String> params = new HashMap<>();
        fcmToken = FirebaseInstanceId.getInstance().getToken();

        PreferenceUtils.setStringPreference(GetOptionalProfilePic.this,
                PreferenceUtils.PREF_FCM_TOKEN, fcmToken);

        params.put("name", rollNo);
        params.put("rollNo", name);
        params.put("email", email);
        params.put("contact", contact);
        params.put("fcmToken", fcmToken);

        Log.e(TAG,params.toString());

        /***********************************************
         */
//        storeUserInformationAndProceed("444", name, email,
//                contact, "uhcbjhb7zdckhDC87", rollNo);

        /***********************************************/
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST,
                REGISTER_URL, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject resp) {
                try {
                    String status = resp.getString("status");
                    Log.e(TAG, resp.toString());
                    if (status.equals("200")) {

//                        PreferenceUtils.setStringPreference(GetOptionalProfilePic.this,
//                                PreferenceUtils.PREF_USER_GLOBAL_ID, userGlobalId);
                        PreferenceUtils.setBooleanPreference(GetOptionalProfilePic.this,
                                PreferenceUtils.PREF_IS_FCM_REGISTERED, true);
                        PreferenceUtils.setBooleanPreference(GetOptionalProfilePic.this,
                                PreferenceUtils.PREF_USER_LOGGED_IN, true);


                        Intent intent = new Intent(GetOptionalProfilePic.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    else if (resp.getString("response").equals("User with same email exists")) {
                        showSnackBar("You entered duplicate details! Please Try again", "RETRY", CASE_REGISTER);

                        picpage.setVisibility(View.VISIBLE);
                        progresslayout.setVisibility(View.GONE);
                    }

                    else {
                        showSnackBar("Some error occured! Please Try again", "RETRY", CASE_REGISTER);

                        picpage.setVisibility(View.VISIBLE);
                        progresslayout.setVisibility(View.GONE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    showSnackBar("Error! Please Try Again! (JsonException)","RETRY",CASE_REGISTER);
                    picpage.setVisibility(View.VISIBLE);
                    progresslayout.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.toString());
                showSnackBar("Network Unreachable!","RETRY",CASE_REGISTER);
                picpage.setVisibility(View.VISIBLE);
                progresslayout.setVisibility(View.GONE);
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

    private void showSnackBar(String msg, String action, final int caseId) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (caseId == CASE_REGISTER) {
                            register(name,rollno, email,contact);
                        }
                    }
                });
        snackbar.setActionTextColor(getResources().getColor(R.color.pink500));
        snackbar.show();
    }




}
