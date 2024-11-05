package com.example.fitpro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangeMoveGoalActivity extends AppCompatActivity {

    // Declaration of UI components and Firebase instances.
    private TextView currentMoveGoalTextView;
    private EditText newMoveGoalEditText; // Input field for new movement goal
    private Button changeButton;
    private ImageView backButton;

    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase Auth instance for user authentication
    private String userId; // Stores the current user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_move_goal);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the current logged-in user
        if (currentUser != null) {
            userId = currentUser.getUid(); // Use user's UID as document ID in Firestore
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return; // Exit if no user is logged in
        }

        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        // Link UI components with their IDs
        currentMoveGoalTextView = findViewById(R.id.textView_currentMoveGoal);
        newMoveGoalEditText = findViewById(R.id.editText_newMoveGoal);
        changeButton = findViewById(R.id.button_change);
        backButton = findViewById(R.id.View_back);

        // Set an OnClickListener for the back button to finish the activity.
        backButton.setOnClickListener(view -> finish());

        // OnClickListener for the change button to update the move goal
        changeButton.setOnClickListener(view -> {
            String newMoveGoalStr = newMoveGoalEditText.getText().toString();
            if (!newMoveGoalStr.isEmpty()) {
                try {
                    int newMoveGoal = Integer.parseInt(newMoveGoalStr); // Parse the input to an integer
                    updateMoveGoal(newMoveGoal); // Update the move goal in Firestore
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid goal format. Please enter a number.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a new move goal.", Toast.LENGTH_SHORT).show();
            }
        });

        loadCurrentMoveGoal(); // Load and display the current move goal
    }

    // Load the current move goal from Firestore and display it
    private void loadCurrentMoveGoal() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long currentMoveGoal = documentSnapshot.getLong("moveGoal");
                if (currentMoveGoal != null) {
                    currentMoveGoalTextView.setText(String.valueOf(currentMoveGoal)); // Display the current goal
                } else {
                    Toast.makeText(this, "Move goal not set.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not found in database.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load move goal.", Toast.LENGTH_SHORT).show());
    }

    // Update the move goal in Firestore and update UI accordingly
    private void updateMoveGoal(int newMoveGoal) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.update("moveGoal", newMoveGoal)
                .addOnSuccessListener(aVoid -> {
                    currentMoveGoalTextView.setText(String.valueOf(newMoveGoal)); // Update displayed goal
                    Toast.makeText(this, "Move goal updated successfully.", Toast.LENGTH_SHORT).show();
                    newMoveGoalEditText.setText(""); // Clear the input field
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update move goal.", Toast.LENGTH_SHORT).show());
    }
}
