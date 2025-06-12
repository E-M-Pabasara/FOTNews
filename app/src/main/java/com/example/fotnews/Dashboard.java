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

    private String currentUsername; // Declare variables to hold the user data
    private String currentEmail; // Declare variables to hold the user data
    private String currentUserKey; // Declare variables to hold the user data

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

        // Retrieve user data passed from SignUp/SignIn activity
        Intent intentFromCallingActivity = getIntent();
        if (intentFromCallingActivity != null) {
            currentUsername = intentFromCallingActivity.getStringExtra("username");
            currentEmail = intentFromCallingActivity.getStringExtra("email");
            currentUserKey = intentFromCallingActivity.getStringExtra("userKey");
        }

        // Button references
        ImageButton backButton = findViewById(R.id.back_button);
        MaterialButton settingsButton = findViewById(R.id.settings_button);
        MaterialButton profileButton = findViewById(R.id.profile_button);

        // Category buttons
        MaterialButton sportsButton = findViewById(R.id.btn_sports);
        MaterialButton academicButton = findViewById(R.id.btn_academic);
        MaterialButton eventsButton = findViewById(R.id.btn_events);

        // Navigate to SplashScreen on back button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, SplashScreen.class);
                startActivity(intent);
                finish(); // Optional: closes current activity
            }
        });

        // Navigate to DevInformation on settings button click
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, DevInformation.class);
                startActivity(intent);
            }
        });

        // Navigate to UserInformation on profile button click
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, UserInfo.class);
                // Pass the retrieved user data to UserInfo activity
                intent.putExtra("username", currentUsername);
                intent.putExtra("email", currentEmail);
                intent.putExtra("userKey", currentUserKey);
                startActivity(intent);
            }
        });

        // Sports category button click
        sportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, CategoryNewsActivity.class);
                intent.putExtra("category", "sports");
                intent.putExtra("categoryTitle", "Sports");
                startActivity(intent);
            }
        });

        // Academic category button click
        academicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, CategoryNewsActivity.class);
                intent.putExtra("category", "academic");
                intent.putExtra("categoryTitle", "Academic");
                startActivity(intent);
            }
        });

        // Events category button click
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, CategoryNewsActivity.class);
                intent.putExtra("category", "events");
                intent.putExtra("categoryTitle", "Events");
                startActivity(intent);
            }
        });
    }
}