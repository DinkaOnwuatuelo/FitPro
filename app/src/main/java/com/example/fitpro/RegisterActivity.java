package com.example.fitpro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    // UI components
    private RadioButton selectedGenderRadioButton;
    private RadioGroup radioGroupRegisterGender;
    private EditText editTextRegisterFullName, editTextRegisterEmailAddress, editTextRegisterDoB, editTextRegisterPassword, editTextRegisterConfirmPassword;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up click listener for the "Login" TextView
        TextView textViewLoginSpecific = findViewById(R.id.textViewLoginSpecific);
        textViewLoginSpecific.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open LoginActivity when "Login" is clicked
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Display a toast to inform the user about the registration process
        Toast.makeText(RegisterActivity.this, "You can now register.", Toast.LENGTH_LONG).show();

        // Initialize UI components
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmailAddress = findViewById(R.id.editText_register_email_address);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterPassword = findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPassword = findViewById(R.id.editText_register_confirm_password);
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        // Set up click listener for the "Continue" button
        Button buttonContinue = findViewById(R.id.button_continue);
        buttonContinue.setOnClickListener(v -> {
            processRegistration(); // Start the registration process when the button is clicked
        });
    }

    // Method to handle the registration process
    private void processRegistration() {
        // Get the selected gender from RadioGroup
        int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
        selectedGenderRadioButton = findViewById(selectedGenderId);

        // Get input values from EditText fields
        String textFullName = editTextRegisterFullName.getText().toString().trim();
        String textEmailAddress = editTextRegisterEmailAddress.getText().toString().trim();
        String textDoB = editTextRegisterDoB.getText().toString().trim();
        String textPassword = editTextRegisterPassword.getText().toString().trim();
        String textConfirmPassword = editTextRegisterConfirmPassword.getText().toString().trim();
        String textGender = (selectedGenderRadioButton != null) ? selectedGenderRadioButton.getText().toString() : "";

        // Validate inputs and proceed with registration
        registerUser(textFullName, textEmailAddress, textDoB, textGender, textPassword, textConfirmPassword);
    }

    // Method to register user with Firebase Authentication
    private void registerUser(String textFullName, String textEmailAddress, String textDoB, String textGender, String textPassword, String textConfirmPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create user with email and password
        auth.createUserWithEmailAndPassword(textEmailAddress, textPassword).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Send email verification
                FirebaseUser firebaseUser = Objects.requireNonNull(auth.getCurrentUser());
                firebaseUser.sendEmailVerification();

                // Saving user details in Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = firebaseUser.getUid();
                User newUser = new User(textFullName, textEmailAddress, textDoB, textGender, 0, 0); // Assuming default height and weight as 0 for now
                db.collection("users").document(userId).set(newUser).addOnSuccessListener(aVoid -> {
                    // User details saved successfully, proceed to AdditionalInfoActivity
                    Intent intent = new Intent(RegisterActivity.this, AdditionalInfoActivity.class);
                    startActivity(intent);
                    finish();
                }).addOnFailureListener(e -> {
                    // Handle failure to save user details
                    Toast.makeText(RegisterActivity.this, "Failed to save user details.", Toast.LENGTH_SHORT).show();
                });

            } else {
                // Handle registration errors
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthWeakPasswordException e) {
                    editTextRegisterPassword.setError("Your password is weak. Kindly use a mix of alphabets, numbers and special characters");
                    editTextRegisterPassword.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    editTextRegisterEmailAddress.setError("Your email is invalid or already in use, kindly re-enter.");
                    editTextRegisterEmailAddress.requestFocus();
                } catch (FirebaseAuthUserCollisionException e) {
                    editTextRegisterEmailAddress.setError("A User is already registered with this email. Use another email");
                    editTextRegisterEmailAddress.requestFocus();
                } catch (Exception e) {
                    // Log and display other exceptions
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
