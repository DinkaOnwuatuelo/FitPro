package com.example.fitpro;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class HealthDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the user interface layout for this Activity.
        setContentView(R.layout.activity_health_details);

        // Get the current logged-in Firebase user.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // If a user is logged in, fetch their health details using their unique userID.
            String userId = currentUser.getUid();
            fetchUserDetails(userId);
        } else {
            // Log a message if no user is found logged in.
            Log.d("HealthDetailsActivity", "No user logged in.");
        }

        // Find the button_done and backView from the layout and set click listeners on them.
        Button buttonDone = findViewById(R.id.button_done);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity, useful for returning to the previous screen.
                finish();
            }
        });

        ImageView backView = findViewById(R.id.backView);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Also closes this activity, useful for returning to the previous screen.
                finish();
            }
        });
    }

    // Method to fetch user details from Firestore based on the userId.
    private void fetchUserDetails(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Listen for real-time updates for the user's document in the "users" collection.
        db.collection("users").document(userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Log an error if the snapshot listener fails.
                            Log.w("HealthDetailsActivity", "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Convert the snapshot into a User object if it is not null and exists.
                            User user = snapshot.toObject(User.class);
                            if (user != null) {
                                // Update the UI with the user's health details.
                                updateUI(user);
                            }
                        } else {
                            // Log if the current data is null.
                            Log.d("HealthDetailsActivity", "Current data: null");
                        }
                    }
                });
    }

    // Method to update the UI with the user's health details.
    private void updateUI(User user) {
        // Find the TextViews in the layout and set their text with the user's health details.
        TextView textViewDateOfBirth = findViewById(R.id.textView_dob);
        textViewDateOfBirth.setText(user.getDateOfBirth());

        TextView textViewGender = findViewById(R.id.textView_gender);
        textViewGender.setText(user.getGender());

        TextView textViewHeight = findViewById(R.id.textView_height);
        textViewHeight.setText(String.format("%.2f cm", user.getHeight()));

        TextView textViewWeight = findViewById(R.id.textView_weight);
        textViewWeight.setText(String.format("%.2f kg", user.getWeight()));
    }
}
