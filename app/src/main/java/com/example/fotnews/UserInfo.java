package com.example.fotnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText; // Import TextInputEditText
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserInfo extends AppCompatActivity {

    private static final String TAG = "UserInfoActivity";

    private TextView userName;
    private TextView userEmail;
    private ImageButton backButton;
    private MaterialButton btnEditInfo;
    private MaterialButton btnSignOut;
    private LinearLayout signOutConfirmationLayout;
    private MaterialButton btnYesSignOut;
    private MaterialButton btnNoSignOut;
    private View overlay;

    private LinearLayout editInfoLayout;
    private TextInputEditText editUsernameField;
    private TextInputEditText editPasswordField;
    private TextInputLayout editUsernameLayout;
    private TextInputLayout editPasswordLayout;
    private MaterialButton btnOkEdit;
    private MaterialButton btnCancelEdit;

    private DatabaseReference databaseReference;

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


        signOutConfirmationLayout = findViewById(R.id.signOutConfirmationLayout);
        btnYesSignOut = findViewById(R.id.btnYesSignOut);
        btnNoSignOut = findViewById(R.id.btnNoSignOut);
        overlay = findViewById(R.id.overlay);


        editInfoLayout = findViewById(R.id.editInfoLayout);
        editUsernameField = findViewById(R.id.editUsernameField);
        editPasswordField = findViewById(R.id.editPasswordField);
        editUsernameLayout = findViewById(R.id.editUsernameLayout);
        editPasswordLayout = findViewById(R.id.editPasswordLayout);
        btnOkEdit = findViewById(R.id.btnOkEdit);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);


        signOutConfirmationLayout.setVisibility(View.GONE);
        editInfoLayout.setVisibility(View.GONE);
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


        if (currentUsername == null || currentEmail == null) {
            getUserDataFromPreferences();
        }


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

            userName.setText("User Name : " + currentUsername);
            userEmail.setText("Email : " + currentEmail);


            saveUserDataToPreferences();
        } else if (currentUsername != null) {

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


                            userName.setText("User Name : " + user.getUsername());
                            userEmail.setText("Email : " + user.getEmail());


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

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(UserInfo.this, Dashboard.class);
                startActivity(intent);
            }
        });


        btnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editUsernameField.setText(currentUsername);
                editPasswordField.setText(""); // Password should not be pre-filled for security

                editInfoLayout.setVisibility(View.VISIBLE);
                overlay.setVisibility(View.VISIBLE); // Show the overlay
            }
        });


        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutConfirmationLayout.setVisibility(View.VISIBLE);
                overlay.setVisibility(View.VISIBLE); // Show the overlay
            }
        });


        btnYesSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                signOutConfirmationLayout.setVisibility(View.GONE);
                overlay.setVisibility(View.GONE);
            }
        });


        btnNoSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutConfirmationLayout.setVisibility(View.GONE);
                overlay.setVisibility(View.GONE);
            }
        });


        btnOkEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });


        btnCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editInfoLayout.setVisibility(View.GONE);
                overlay.setVisibility(View.GONE);
                clearEditErrors();
            }
        });


        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signOutConfirmationLayout.getVisibility() == View.VISIBLE) {
                    signOutConfirmationLayout.setVisibility(View.GONE);
                }
                if (editInfoLayout.getVisibility() == View.VISIBLE) {
                    editInfoLayout.setVisibility(View.GONE);
                    clearEditErrors();
                }
                overlay.setVisibility(View.GONE);
            }
        });
    }

    private void updateUserInfo() {
        String newUsername = editUsernameField.getText().toString().trim();
        String newPassword = editPasswordField.getText().toString().trim();

        clearEditErrors();

        if (!validateEditInputs(newUsername, newPassword)) {
            return;
        }


        if (currentUserKey != null) {
            DatabaseReference userRef = databaseReference.child(currentUserKey);
            Map<String, Object> updates = new HashMap<>();


            if (!newUsername.equals(currentUsername)) {

                Query usernameQuery = databaseReference.orderByChild("username").equalTo(newUsername);
                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            editUsernameLayout.setError("Username already exists.");
                        } else {

                            updates.put("username", newUsername);
                            if (!newPassword.isEmpty()) {
                                updates.put("password", newPassword); // Hash in real app
                            }
                            applyUpdates(userRef, updates, newUsername, newPassword);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserInfo.this, "Error checking username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {

                if (!newPassword.isEmpty()) {
                    updates.put("password", newPassword);
                }
                applyUpdates(userRef, updates, newUsername, newPassword);
            }

        } else {
            Toast.makeText(this, "User key not found. Cannot update.", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyUpdates(DatabaseReference userRef, Map<String, Object> updates, String newUsername, String newPassword) {
        if (!updates.isEmpty()) {
            userRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UserInfo.this, "User information updated successfully!", Toast.LENGTH_SHORT).show();
                        editInfoLayout.setVisibility(View.GONE);
                        overlay.setVisibility(View.GONE);
                        clearEditErrors();

                        // Update current local data and SharedPreferences
                        currentUsername = newUsername; // Update local username
                        if (!newPassword.isEmpty()) {
                            // Password is updated, but we don't store it locally for display
                        }
                        saveUserDataToPreferences(); // Save updated username to preferences

                        // Refresh displayed data on UI
                        userName.setText("User Name : " + currentUsername);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UserInfo.this, "Failed to update user information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {

            editInfoLayout.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
            clearEditErrors();
        }
    }


    private boolean validateEditInputs(String newUsername, String newPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(newUsername)) {
            editUsernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else if (newUsername.length() < 3) {
            editUsernameLayout.setError("Username must be at least 3 characters");
            isValid = false;
        } else if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
            editUsernameLayout.setError("Username can only contain letters, numbers, and underscores");
            isValid = false;
        }


        if (!newPassword.isEmpty() && newPassword.length() < 6) {
            editPasswordLayout.setError("Password must be at least 6 characters (if changing)");
            isValid = false;
        }

        return isValid;
    }

    private void clearEditErrors() {
        editUsernameLayout.setError(null);
        editPasswordLayout.setError(null);
    }

    private void signOut() {

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();


        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(UserInfo.this, SignIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentUsername != null) {
            fetchUserDataFromFirebase();
        }
    }

    @Override
    public void onBackPressed() {

        if (signOutConfirmationLayout.getVisibility() == View.VISIBLE) {
            signOutConfirmationLayout.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
        } else if (editInfoLayout.getVisibility() == View.VISIBLE) {
            editInfoLayout.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
            clearEditErrors();
        } else {

            Intent intent = new Intent(UserInfo.this, Dashboard.class);
            startActivity(intent);
            finish();
        }
    }
}