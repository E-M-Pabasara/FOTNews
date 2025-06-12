package com.example.fotnews;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class Dashboard extends AppCompatActivity {

    private String currentUsername;
    private String currentEmail;
    private String currentUserKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Intent intentFromCallingActivity = getIntent();
        if (intentFromCallingActivity != null) {
            currentUsername = intentFromCallingActivity.getStringExtra("username");
            currentEmail = intentFromCallingActivity.getStringExtra("email");
            currentUserKey = intentFromCallingActivity.getStringExtra("userKey");
        }


        ImageButton backButton = findViewById(R.id.back_button);
        MaterialButton settingsButton = findViewById(R.id.settings_button);
        MaterialButton profileButton = findViewById(R.id.profile_button);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, SplashScreen.class);
                startActivity(intent);
                finish();
            }
        });


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, DevInformation.class);
                startActivity(intent);
            }
        });


        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, UserInfo.class);
                intent.putExtra("username", currentUsername);
                intent.putExtra("email", currentEmail);
                intent.putExtra("userKey", currentUserKey);
                startActivity(intent);
            }
        });
    }
}