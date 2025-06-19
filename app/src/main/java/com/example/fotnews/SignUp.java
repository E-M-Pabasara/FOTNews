package com.example.fotnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {


    private TextInputEditText usernameField, emailField, passwordField, confirmPasswordField;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private MaterialButton signUpButton;


    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");


        initializeViews();


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void initializeViews() {
        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);

        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        signUpButton = findViewById(R.id.signUpButton);
    }

    private void registerUser() {

        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();


        clearErrors();


        if (!validateInputs(username, email, password, confirmPassword)) {
            return;
        }


        signUpButton.setEnabled(false);
        signUpButton.setText("Creating Account...");


        checkUsernameExists(username, email, password);
    }

    private void checkUsernameExists(String username, String email, String password) {
        Query usernameQuery = databaseReference.orderByChild("username").equalTo(username);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    usernameLayout.setError("Username already exists. Please choose another.");
                    resetButtonState();
                } else {

                    checkEmailExists(username, email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUp.this, "Error checking username: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                resetButtonState();
            }
        });
    }

    private void checkEmailExists(String username, String email, String password) {
        Query emailQuery = databaseReference.orderByChild("email").equalTo(email);

        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    emailLayout.setError("Email already registered. Please use another email.");
                    resetButtonState();
                } else {

                    createUserAccount(username, email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUp.this, "Error checking email: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                resetButtonState();
            }
        });
    }

    private void createUserAccount(String username, String email, String password) {

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", password);
        userData.put("timestamp", System.currentTimeMillis());


        String userId = databaseReference.push().getKey();


        databaseReference.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(SignUp.this, "Account created successfully!", Toast.LENGTH_SHORT).show();


                    saveUserSession(username, email, userId);


                    navigateToDashboard(username, email, userId);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(SignUp.this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetButtonState();
                });
    }

    private void saveUserSession(String username, String email, String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("userKey", userId);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void navigateToDashboard(String username, String email, String userId) {
        Intent intent = new Intent(SignUp.this, Dashboard.class);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        intent.putExtra("userKey", userId);
        intent.putExtra("isLoggedIn", true);

        startActivity(intent);
        finish();
    }

    private void resetButtonState() {
        signUpButton.setEnabled(true);
        signUpButton.setText("Sign Up");
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        boolean isValid = true;


        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            usernameLayout.setError("Username must be at least 3 characters");
            isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameLayout.setError("Username can only contain letters, numbers, and underscores");
            isValid = false;
        }


        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            isValid = false;
        }


        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private void clearFields() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
}