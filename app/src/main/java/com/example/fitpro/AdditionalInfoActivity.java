package com.example.fitpro;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdditionalInfoActivity extends AppCompatActivity {

    // Declare UI components and Firestore instance.
    private EditText heightInput, weightInput;
    private Button registerButton;
    private FirebaseFirestore firestore; // Firestore database instance.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_info);

        // Initialize the Firestore instance.
        firestore = FirebaseFirestore.getInstance();

        // Link UI components with their respective IDs in the layout file.
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        registerButton = findViewById(R.id.registerButton);

        // Set an OnClickListener for the register button.
        registerButton.setOnClickListener(v -> {
            // Retrieve the height and weight input as strings.
            String heightStr = heightInput.getText().toString();
            String weightStr = weightInput.getText().toString();

            // Check if the height and weight strings are not empty.
            if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
                try {
                    // Parse the height and weight strings to double values.
                    double height = Double.parseDouble(heightStr);
                    double weight = Double.parseDouble(weightStr);

                    // Get the current logged-in Firebase user.
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        // Get the user's unique ID.
                        String userId = user.getUid();
                        // Reference to the user's document in the "users" collection in Firestore.
                        DocumentReference userRef = firestore.collection("users").document(userId);

                        // Update the user's height and weight in Firestore.
                        userRef.update("height", height, "weight", weight)
                                .addOnSuccessListener(aVoid -> {
                                    // Notify the user of success and navigate to the SetMoveGoalActivity.
                                    Toast.makeText(AdditionalInfoActivity.this, "User information updated successfully", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(AdditionalInfoActivity.this, SetMoveGoalActivity.class);
                                    startActivity(intent);
                                    finish(); // Close the current activity.
                                })
                                .addOnFailureListener(e -> {
                                    // Notify the user of failure.
                                    Toast.makeText(AdditionalInfoActivity.this, "Failed to update user information", Toast.LENGTH_SHORT).show();
                                });
                    }
                } catch (NumberFormatException e) {
                    // Catch parsing errors and notify the user.
                    Toast.makeText(AdditionalInfoActivity.this, "Please enter valid numbers for height and weight", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Notify the user if height or weight fields are empty.
                Toast.makeText(AdditionalInfoActivity.this, "Height and Weight cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
