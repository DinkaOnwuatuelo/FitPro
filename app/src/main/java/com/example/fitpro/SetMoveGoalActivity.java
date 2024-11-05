package com.example.fitpro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetMoveGoalActivity extends AppCompatActivity {

    // UI components for entering and setting the daily calorie goal.
    private EditText dailyCalorieGoalInput;
    private Button setGoalButton;
    // Instance of Firestore to interact with Firebase database.
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_move_goal);

        // Initialize UI components.
        dailyCalorieGoalInput = findViewById(R.id.dailyStepGoalInput);
        setGoalButton = findViewById(R.id.setGoalButton);

        // Initialize Firestore instance.
        firestore = FirebaseFirestore.getInstance();

        // Fetch the current move goal from Firestore when the activity starts.
        retrieveDailyMoveGoal();

        // Set a click listener on the "Set Goal" button to update the move goal in Firestore.
        setGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calls the method to set the new move goal.
                setDailyMoveGoal();
            }
        });
    }

    // Method to fetch the current move goal from Firestore and display it in the EditText.
    private void retrieveDailyMoveGoal() {
        // Get the current Firebase user.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // If a user is logged in, fetch their move goal from Firestore.
            String userId = user.getUid();
            DocumentReference goalRef = firestore.collection("users").document(userId);

            // Retrieve the document and set the EditText's content to the fetched goal.
            goalRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Extract the goal value and update the EditText.
                    Long goal = documentSnapshot.getLong("dailyMoveGoal");
                    dailyCalorieGoalInput.setText(goal != null ? String.valueOf(goal) : "");
                }
            }).addOnFailureListener(e -> Log.w("FirestoreError", "Error fetching move goal", e));
        }
    }

    // Method to set or update the daily move goal in Firestore.
    private void setDailyMoveGoal() {
        // Get the goal from the EditText and trim any leading or trailing spaces.
        String goalStr = dailyCalorieGoalInput.getText().toString().trim();
        if (!goalStr.isEmpty()) {
            // Parse the goal value to an integer.
            int goal = Integer.parseInt(goalStr);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                // If a user is logged in, update their move goal in Firestore.
                String userId = user.getUid();
                DocumentReference goalRef = firestore.collection("users").document(userId);

                // Update the move goal in Firestore.
                goalRef.update("dailyMoveGoal", goal)
                        .addOnSuccessListener(aVoid -> {
                            // Show a success message and navigate to the SummaryPageActivity.
                            Toast.makeText(SetMoveGoalActivity.this, "Daily move goal set successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SetMoveGoalActivity.this, SummaryPageActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(SetMoveGoalActivity.this, "Failed to set move goal", Toast.LENGTH_SHORT).show());
            }
        } else {
            // Prompt the user to enter a goal if the EditText is empty.
            Toast.makeText(this, "Please enter a goal", Toast.LENGTH_SHORT).show();
        }
    }
}
