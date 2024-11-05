package com.example.fitpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // Declare UI components and Firebase authentication object.
    private EditText editTextLoginEmail, editTextLoginPassword;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the layout defined in activity_login.xml.
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth instance.
        authProfile = FirebaseAuth.getInstance();

        // Link UI components with their respective IDs in the layout file.
        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextLoginPassword = findViewById(R.id.editText_login_password);
        TextView textViewRegisterSpecific = findViewById(R.id.textViewRegisterSpecific);
        TextView textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        // Set click listener for the "Register" TextView to navigate to the RegisterActivity.
        textViewRegisterSpecific.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Handle login button click events.
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(v -> {
            // Retrieve user input and trim any leading or trailing whitespace.
            String textEmail = editTextLoginEmail.getText().toString().trim();
            String textPassword = editTextLoginPassword.getText().toString().trim();

            // Validate user input and show error messages if necessary.
            if (TextUtils.isEmpty(textEmail)) {
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                editTextLoginEmail.setError("Email is required");
                editTextLoginEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                Toast.makeText(LoginActivity.this, "Please re-enter your email", Toast.LENGTH_SHORT).show();
                editTextLoginEmail.setError("Valid email is required");
                editTextLoginEmail.requestFocus();
            } else if (TextUtils.isEmpty(textPassword)) {
                Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                editTextLoginPassword.setError("Password is required");
                editTextLoginPassword.requestFocus();
            } else {
                // Attempt to login the user with the provided credentials.
                loginUser(textEmail, textPassword);
            }
        });

        // Set click listener for the "Forgot Password" TextView to show reset password dialog.
        textViewForgotPassword.setOnClickListener(v -> showResetPasswordDialog());
    }

    // Method to authenticate user with email and password.
    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Navigate to SummaryPageActivity on successful login.
                    Toast.makeText(LoginActivity.this, "You are logged in now", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, SummaryPageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Show error message on login failure.
                    Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Show a dialog for the user to reset their password.
    private void showResetPasswordDialog() {
        final EditText resetMail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link.");
        passwordResetDialog.setView(resetMail);

        // Handle click event for the "Send" button in the dialog.
        passwordResetDialog.setPositiveButton("Send", (dialog, which) -> {
            String mail = resetMail.getText().toString();
            // Attempt to send a password reset email.
            authProfile.sendPasswordResetEmail(mail).addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error! Reset link is not sent " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

}