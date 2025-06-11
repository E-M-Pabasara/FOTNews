package com.example.fotnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout; // Import LinearLayout
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
    private LinearLayout signOutConfirmationLayout; // Reference to the custom pop-up layout
    private MaterialButton btnYesSignOut; // Reference to the YES button
    private MaterialButton btnNoSignOut; // Reference to the NO button
    private View overlay; // Reference to the dimming overlay

    // Firebase
    private DatabaseReference databaseReference;

    // User data
    private String currentUsername;
    private String currentEmail;
    private String currentUserKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info); //

        initializeViews(); // Initializes UI components, including the pop-up layout and its buttons
        initializeFirebase();
        getUserDataFromIntent();
        displayUserData();
        setupClickListeners(); // Sets up click listeners for all buttons, including the pop-up buttons
    }

    private void initializeViews() {
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        backButton = findViewById(R.id.back_button);
        btnEditInfo = findViewById(R.id.btnEditInfo);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Initialize custom pop-up related views
        signOutConfirmationLayout = findViewById(R.id.signOutConfirmationLayout);
        btnYesSignOut = findViewById(R.id.btnYesSignOut);
        btnNoSignOut = findViewById(R.id.btnNoSignOut);
        overlay = findViewById(R.id.overlay); // Initialize the overlay view

        // Initially hide the confirmation layout and overlay
        signOutConfirmationLayout.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
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
                // Modified: Navigate to Dashboard instead of just onBackPressed()
                Intent intent = new Intent(UserInfo.this, Dashboard.class);
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

        // Sign Out button: Show the custom pop-up and overlay
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutConfirmationLayout.setVisibility(View.VISIBLE);
                overlay.setVisibility(View.VISIBLE); // Show the overlay
            }
        });

        // "YES" button in the custom sign-out confirmation pop-up
        btnYesSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut(); // Perform actual sign-out
                signOutConfirmationLayout.setVisibility(View.GONE); // Hide pop-up
                overlay.setVisibility(View.GONE); // Hide overlay
            }
        });

        // "NO" button in the custom sign-out confirmation pop-up
        btnNoSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutConfirmationLayout.setVisibility(View.GONE); // Hide pop-up
                overlay.setVisibility(View.GONE); // Hide overlay
            }
        });

        // Optionally, hide pop-up if overlay is clicked
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutConfirmationLayout.setVisibility(View.GONE); // Hide pop-up
                overlay.setVisibility(View.GONE); // Hide overlay
            }
        });
    }

    private void signOut() {
        // Clear user session from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clears all data
        editor.apply();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login screen
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(UserInfo.this, SignIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
        startActivity(intent);
        finish(); // Finish current activity
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this activity
        if (currentUsername != null) {
            fetchUserDataFromFirebase();
        }
    }

    @Override
    public void onBackPressed() {
        // If the pop-up is visible, hide it first
        if (signOutConfirmationLayout.getVisibility() == View.VISIBLE) {
            signOutConfirmationLayout.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE); // Hide overlay
        } else {
            // Otherwise, proceed with default back button behavior (go to Dashboard)
            Intent intent = new Intent(UserInfo.this, Dashboard.class);
            startActivity(intent);
            super.onBackPressed(); // Call super to ensure proper back stack handling
        }
    }
}