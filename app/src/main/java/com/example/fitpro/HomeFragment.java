package com.example.fitpro;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    private static final String PREFS_NAME = "StepCounterPrefs";
    private static final String PREF_STEP_COUNT_KEY = "stepCount";
    private static final String PREF_STEP_DATE_KEY = "stepDate";

    private TextView dateTitle, stepCountTextView, stepGoalTextView, distanceTextView, timeTextView, caloriesTextView;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isSensorPresent = false;
    private int totalSteps = 0;
    private int stepsSinceReboot = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dateTitle = view.findViewById(R.id.textView_dateTitle);
        stepCountTextView = view.findViewById(R.id.textView_stepCount);
        stepGoalTextView = view.findViewById(R.id.textView_stepGoal);
        distanceTextView = view.findViewById(R.id.textView_distanceRange);
        timeTextView = view.findViewById(R.id.textView_time);
        caloriesTextView = view.findViewById(R.id.textView_calories);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            isSensorPresent = false;
        }

        loadStepData();
        updateUI();
        fetchStepGoal(); // Fetch the step goal from Firestore
        return view;
    }

    private void fetchStepGoal() {
        String userId = getCurrentUserId(); // Ensures this method returns the correct user ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Number stepGoalNumber = document.getLong("stepGoal"); // Use Number to avoid potential null
                        if (stepGoalNumber != null) {
                            final long stepGoal = stepGoalNumber.longValue();
                            // Ensure UI update is run on the main thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stepGoalTextView.setText("Step Goal: " + stepGoal);
                                }
                            });
                        } else {
                            Log.d("HomeFragment", "Step goal is null");
                        }
                    } else {
                        Log.d("HomeFragment", "No such document");
                    }
                } else {
                    Log.d("HomeFragment", "get failed with ", task.getException());
                }
            }
        });
    }


    private void loadStepData() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastUpdateDate = prefs.getString(PREF_STEP_DATE_KEY, "");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!lastUpdateDate.equals(currentDate)) {
            stepsSinceReboot = 0;
            totalSteps = 0;
        } else {
            stepsSinceReboot = prefs.getInt(PREF_STEP_COUNT_KEY, 0);
        }
    }

    // Register the sensor listener when the fragment resumes.
    @Override
    public void onResume() {
        super.onResume();
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // Unregister the sensor listener when the fragment is not visible.
    @Override
    public void onPause() {
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this, stepCounterSensor);
        }
    }


    // Handle step counter updates from the sensor.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int lastSavedStepCount = prefs.getInt(PREF_STEP_COUNT_KEY, 0);
            String lastUpdateDate = prefs.getString(PREF_STEP_DATE_KEY, "");
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (!lastUpdateDate.equals(currentDate)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PREF_STEP_COUNT_KEY, (int) event.values[0]);
                editor.putString(PREF_STEP_DATE_KEY, currentDate);
                editor.apply();

                stepsSinceReboot = (int) event.values[0];
                totalSteps = 0; // Reset daily step count
            } else {
                totalSteps = (int) event.values[0] - lastSavedStepCount;
            }

            calculateAndUpdateMetrics(); // Perform calculations and update UI
        }
    }


    // Calculate distance, time, and calories based on the step count and update the UI.
    private void calculateAndUpdateMetrics() {
        // Distance calculation (Assuming an average stride length of 0.762 meters)
        float strideLength = 0.762f; // meters
        float distanceInMeters = totalSteps * strideLength;
        float distanceInKm = distanceInMeters / 1000;

        // Time calculation (Assuming an average pace of 500 steps in 5 minutes)
        float minutesTaken = (totalSteps / 500f) * 5;

        // Calories calculation (Assuming 0.04 calories burned per step)
        float caloriesBurned = totalSteps * 0.04f;

        // Updating the UI with the calculated values
        stepCountTextView.setText(totalSteps + "\nStep Count");
        distanceTextView.setText(String.format(Locale.getDefault(), "%.2f Km\nDistance", distanceInKm));
        timeTextView.setText(String.format(Locale.getDefault(), "%.2f Minutes\nTime", minutesTaken));
        caloriesTextView.setText(String.format(Locale.getDefault(), "%.2f Calories\nKcal", caloriesBurned));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Log a message when the sensor's accuracy changes.
        Log.d("SensorAccuracy", "Accuracy of sensor " + sensor.getName() + " has changed to " + accuracy);
    }


    private void updateUI() {
        // Display the current date and initialize data in UI components
        String currentDate = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date());
        dateTitle.setText(currentDate + "\nYour Daily Activity");

        // Initial text setting, these will be updated in calculateAndUpdateMetrics method
        stepCountTextView.setText(totalSteps + "\nStep Count");
        distanceTextView.setText(String.format(Locale.getDefault(), "%.2f Km\nDistance", 0.0));
        timeTextView.setText(String.format(Locale.getDefault(), "%.2f Minutes\nTime", 0.0));
        caloriesTextView.setText(String.format(Locale.getDefault(), "%.2f Calories\nKcal", 0.0));
    }

    // Add the method to get the current user's ID (for Firestore)
    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid(); // Returns the unique user ID
        } else {
            // User not logged in or session expired
            return null; // Consider handling this case appropriately
        }
    }
}



