package com.example.fotnews;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    private TextInputEditText usernameField, passwordField;
    private TextInputLayout usernameInputLayout, passwordInputLayout;
    private MaterialButton signInButton, signUpButton;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");

        initializeViews();

        setupClickListeners();
    }

    private void initializeViews() {
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);

        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
    }

    private void setupClickListeners() {

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    private void signInUser() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        clearErrors();

        if (!validateInputs(username, password)) {
            return;
        }


        signInButton.setEnabled(false);
        signInButton.setText("Signing In...");


        loginWithUsername(username, password);
    }

    private void loginWithUsername(String username, String password) {
        Query query = databaseReference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean loginSuccess = false;
                    String email = "";
                    String userKey = "";


                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        email = userSnapshot.child("email").getValue(String.class);
                        userKey = userSnapshot.getKey();

                        if (storedPassword != null && storedPassword.equals(password)) {
                            loginSuccess = true;
                            break;
                        }
                    }

                    if (loginSuccess) {

                        onLoginSuccess(username, email, userKey);
                    } else {
                        // Wrong password
                        onLoginFailure("Incorrect password");
                    }
                } else {
                    // Username not found
                    onLoginFailure("Username not found. Please check your username or sign up.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onLoginFailure("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void onLoginSuccess(String username, String email, String userKey) {
        Toast.makeText(SignIn.this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity
        Intent intent = new Intent(SignIn.this, MainActivity.class);

        // Pass user data to MainActivity
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        intent.putExtra("userKey", userKey);
        intent.putExtra("isLoggedIn", true);

        startActivity(intent);
        finish(); // Close SignIn activity so user can't go back
    }

    private void onLoginFailure(String errorMessage) {
        Toast.makeText(SignIn.this, errorMessage, Toast.LENGTH_LONG).show();

        // Reset button state
        signInButton.setEnabled(true);
        signInButton.setText("Sign In");

        // Clear password field for security
        passwordField.setText("");
    }

    private boolean validateInputs(String username, String password) {
        boolean isValid = true;

        // Validate username
        if (TextUtils.isEmpty(username)) {
            usernameInputLayout.setError("Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            usernameInputLayout.setError("Username must be at least 3 characters");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear any previous errors when user returns to this activity
        clearErrors();
        // Reset button state
        signInButton.setEnabled(true);
        signInButton.setText("Sign In");
    }
}