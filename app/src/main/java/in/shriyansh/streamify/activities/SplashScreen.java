package in.shriyansh.streamify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.PreferenceUtils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRegistrationAndProceed();
    }

    /**
     * Checks if user already registered and proceeds to suitable activity.
     */
    private void checkRegistrationAndProceed() {
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(Constants.SPLASH_SCREEN_DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    boolean isRegistered = PreferenceUtils.getBooleanPreference(
                        SplashScreen.this, PreferenceUtils.PREF_USER_LOGGED_IN);
                    Intent intent;
                    if (isRegistered)
                        intent = new Intent(SplashScreen.this, MainActivity.class);
                    else
                        intent = new Intent(SplashScreen.this, RegisterActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();
    }
}
