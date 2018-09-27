package in.shriyansh.streamify.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import in.shriyansh.streamify.R;

public class ChooseNotifType extends AppCompatActivity {

    private Button btn_post;
    private Button btn_notif;

    private static final String TAG = "ChooseNotifType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_post_or_event);

        btn_post = findViewById(R.id.btn_edit_post);
        btn_notif = findViewById(R.id.btn_edit_notif);

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseNotifType.this,CreateNewsNotif.class);
                startActivity(intent);
            }
        });

        btn_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChooseNotifType.this, CreateEventNotif.class);
                startActivity(intent);

            }
        });

    }
}
