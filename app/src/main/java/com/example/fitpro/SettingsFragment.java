package com.example.fitpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingsFragment extends Fragment {

    private Switch notificationSwitch;
    private TextView fullNameTextView;
    private ImageView profilePictureImageView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        fullNameTextView = view.findViewById(R.id.textView_name);


        // Click listeners for navigating to other activities
        setupNavigationListeners(view);

        // Logout and Delete Account Listeners
        setupAccountActions(view);

        loadUserProfile();

    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    fullNameTextView.setText(fullName);
                } else {
                    Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show());
        }
    }




    private void setupNavigationListeners(View view) {
        view.findViewById(R.id.health_details_layout).setOnClickListener(v -> startActivity(new Intent(getActivity(), HealthDetailsActivity.class)));
        view.findViewById(R.id.change_move_goal_layout).setOnClickListener(v -> startActivity(new Intent(getActivity(), ChangeMoveGoalActivity.class)));
    }

    private void setupAccountActions(View view) {
        view.findViewById(R.id.textView_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Redirect to login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the activity stack and starts a new task
            startActivity(intent);
        });

        view.findViewById(R.id.textView_delete).setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // First, attempt to delete user data from Firestore
                deleteUserDataFromFirestore(user.getUid()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Then, delete the user from Firebase Auth
                        user.delete().addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                Log.d("SettingsFragment", "User account and data deleted.");
                                // Redirect to login/register screen
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the activity stack and starts a new task
                                startActivity(intent);
                            } else {
                                Log.e("SettingsFragment", "Auth deletion failed: " + authTask.getException());
                                // Handle error - could not delete user from Firebase Auth
                            }
                        });
                    } else {
                        Log.e("SettingsFragment", "Firestore deletion failed: " + task.getException());
                        // Handle error - could not delete user data from Firestore
                    }
                });
            }
        });
    }

    private com.google.android.gms.tasks.Task<Void> deleteUserDataFromFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection("users").document(userId).delete();


    }
}
