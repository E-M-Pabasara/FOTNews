package com.example.fotnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UserInfo extends AppCompatActivity {

    private static final String TAG = "UserInfoActivity";

    // UI Components
    private TextView userName;
    private TextView userEmail;
    private ImageButton backButton;
    private MaterialButton btnEditInfo;
    private MaterialButton btnSignOut;

    // Firebase
    private DatabaseReference databaseReference;

    // User data
    private String currentUsername;
    private String currentEmail;
    private String currentUserKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initializeViews();
        initializeFirebase();
        getUserDataFromIntent();
        displayUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        backButton = findViewById(R.id.back_button);
        btnEditInfo = findViewById(R.id.btnEditInfo);
        btnSignOut = findViewById(R.id.btnSignOut);
    }

    private void initializeFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");
    }

    private void getUserDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            currentUsername = intent.getStringExtra("username");
            currentEmail = intent.getStringExtra("email");
            currentUserKey = intent.getStringExtra("userKey");

            Log.d(TAG, "Received user data - Username: " + currentUsername + ", Email: " + currentEmail);
        }

        // If no data from intent, try to get from SharedPreferences (fallback)
        if (currentUsername == null || currentEmail == null) {
            getUserDataFromPreferences();
        }

        // If still no data, fetch from Firebase
        if (currentUsername == null) {
            Toast.makeText(this, "No user data found. Please login again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void getUserDataFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);
        currentEmail = sharedPreferences.getString("email", null);
        currentUserKey = sharedPreferences.getString("userKey", null);
    }

    private void displayUserData() {
        if (currentUsername != null && currentEmail != null) {
            // Display the data we already have
            userName.setText("User Name : " + currentUsername);
            userEmail.setText("Email : " + currentEmail);

            // Also save to SharedPreferences for future use
            saveUserDataToPreferences();
        } else if (currentUsername != null) {
            // If we only have username, fetch full data from Firebase
            fetchUserDataFromFirebase();
        }
    }

    private void fetchUserDataFromFirebase() {
        userName.setText("Loading...");
        userEmail.setText("Loading...");

        Query query = databaseReference.orderByChild("username").equalTo(currentUsername);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            currentEmail = user.getEmail();
                            currentUserKey = userSnapshot.getKey();

                            // Update UI
                            userName.setText("User Name : " + user.getUsername());
                            userEmail.setText("Email : " + user.getEmail());

                            // Save to preferences
                            saveUserDataToPreferences();

                            Log.d(TAG, "User data fetched successfully from Firebase");
                            break;
                        }
                    }
                } else {
                    Log.w(TAG, "User not found in database");
                    userName.setText("User Name : Not found");
                    userEmail.setText("Email : Not found");
                    Toast.makeText(UserInfo.this, "User data not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch user data", databaseError.toException());
                userName.setText("User Name : Error loading");
                userEmail.setText("Email : Error loading");
                Toast.makeText(UserInfo.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDataToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", currentUsername);
        editor.putString("email", currentEmail);
        editor.putString("userKey", currentUserKey);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfo.this, Dashboard.class); // Changed from onBackPressed()
                startActivity(intent);
            }
        });


        // Edit Information button
        btnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to edit profile activity with user data
                Intent intent = new Intent(UserInfo.this, EditProfile.class);
                intent.putExtra("username", currentUsername);
                intent.putExtra("email", currentEmail);
                intent.putExtra("userKey", currentUserKey);
                startActivity(intent);
            }
        });

        // Sign Out button
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut() {
        // Clear user session from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login screen
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(UserInfo.this, SignIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this activity
        if (currentUsername != null) {
            fetchUserDataFromFirebase();
        }
    }
}