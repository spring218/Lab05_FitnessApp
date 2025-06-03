package com.example.fitnesstracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fitnesstracker.api.GeminiClient;
import com.example.fitnesstracker.api.GeminiRequest;
import com.example.fitnesstracker.api.GeminiResponse;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements BoundFitnessService.StepUpdateListener {
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001;

    private BoundFitnessService fitnessService;
    private boolean isServiceBound = false;
    private boolean isTracking = false;
    private GeminiWorkoutSuggestion geminiWorkoutSuggestion;

    private TextView stepCountText;
    private TextView caloriesText;
    private TextView pointsText;
    private TextView motivationText;
    private ProgressBar stepProgressBar;
    private Button startTrackingButton;
    private Button syncMotivationButton;
    private Button getWorkoutSuggestionButton;

    private WearOSSync wearOSSync;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BoundFitnessService.LocalBinder binder = (BoundFitnessService.LocalBinder) service;
            fitnessService = binder.getService();
            isServiceBound = true;
            
            // Set the step update listener
            fitnessService.setStepUpdateListener(MainActivity.this);
            
            updateFitnessMetrics();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            fitnessService = null;
            isServiceBound = false;
        }
    };

    private BroadcastReceiver stepMotivationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        stepCountText = findViewById(R.id.stepCountText);
        caloriesText = findViewById(R.id.caloriesText);
        pointsText = findViewById(R.id.pointsText);
        motivationText = findViewById(R.id.motivationText);
        stepProgressBar = findViewById(R.id.stepProgressBar);
        startTrackingButton = findViewById(R.id.startTrackingButton);
        syncMotivationButton = findViewById(R.id.syncMotivationButton);
        getWorkoutSuggestionButton = findViewById(R.id.getWorkoutSuggestionButton);

        // Set max progress bar to 10000 steps
        stepProgressBar.setMax(10000);

        // Get global components
        FitnessApplication app = (FitnessApplication) getApplication();
        wearOSSync = app.getWearOSSync();

        // Initialize Gemini Workout Suggestion
        geminiWorkoutSuggestion = new GeminiWorkoutSuggestion(this);

        // Start background and foreground services
        startBackgroundServices();

        // Set up button listeners
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestActivityRecognitionPermission()) {
                    toggleTracking();
                }
            }
        });

        syncMotivationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        getWorkoutSuggestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceBound && fitnessService != null) {
                    int steps = fitnessService.getCurrentSteps();
                    int calories = (int) fitnessService.getCaloriesBurned();
                    int points = fitnessService.getPointsEarned();

                    // Show loading state
                    getWorkoutSuggestionButton.setEnabled(false);
                    motivationText.setText("Getting workout suggestion...");

                    // Create a toast for workout suggestion process
                    Toast workoutToast = Toast.makeText(MainActivity.this, 
                        "Generating workout suggestion...", 
                        Toast.LENGTH_LONG);
                    workoutToast.show();

                    // Get workout suggestion
                    geminiWorkoutSuggestion.getWorkoutSuggestion(
                        steps,
                        calories,
                        points,
                        new GeminiWorkoutSuggestion.WorkoutSuggestionCallback() {
                            @Override
                            public void onSuggestionReceived(final String suggestion) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Display suggestion
                                        motivationText.setText(suggestion);
                                        getWorkoutSuggestionButton.setEnabled(true);

                                        // Show success toast
                                        Toast.makeText(MainActivity.this, 
                                            "🏋️ Personalized Workout Suggestion Ready!", 
                                            Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(final String error) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Fallback to simulator suggestion
                                        String simulatorSuggestion = geminiWorkoutSuggestion.generateSimulatorSuggestion(
                                            steps, calories, points
                                        );
                                        
                                        motivationText.setText(simulatorSuggestion);
                                        getWorkoutSuggestionButton.setEnabled(true);
                                        
                                        // Show a more detailed toast for simulator suggestion
                                        Toast.makeText(MainActivity.this, 
                                            "🔄 Switched to Simulator Workout Suggestion", 
                                            Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    );
                } else {
                    // Handle case when service is not bound
                    Toast.makeText(MainActivity.this, 
                        "❌ Fitness service not available", 
                        Toast.LENGTH_LONG).show();
                }
            }
        });

        // Ensure motivation text is visible and empty initially
        motivationText.setVisibility(View.VISIBLE);
        motivationText.setText("Click 'Get Workout Suggestion' to receive personalized advice");

        // Set up broadcast receiver for step motivations
        stepMotivationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.fitnesstracker.STEP_MOTIVATION".equals(intent.getAction())) {
                    String motivation = intent.getStringExtra("motivation");
                    int recommendedSteps = intent.getIntExtra("recommendedSteps", 10000);

                    // Update motivation text
                    if (motivation != null && !motivation.isEmpty()) {
                        motivationText.setText(motivation);
                    }

                    // Optional: Update progress bar with recommended steps
                    stepProgressBar.setMax(recommendedSteps);
                    
                    // Show a toast with the recommended steps
                    Toast.makeText(MainActivity.this, 
                        "New daily goal: " + recommendedSteps + " steps!", 
                        Toast.LENGTH_LONG).show();
                }
            }
        };

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter("com.example.fitnesstracker.STEP_MOTIVATION");
//        registerReceiver(stepMotivationReceiver, filter);

        // Add reset button
        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before resetting
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Reset Fitness Tracking")
                    .setMessage("Are you sure you want to reset your fitness tracking? This will clear all current progress.")
                    .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetFitnessTracking();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
    }

    private void startBackgroundServices() {
        Intent backgroundServiceIntent = new Intent(this, BackgroundFitnessService.class);
        startService(backgroundServiceIntent);

        Intent foregroundServiceIntent = new Intent(this, ForegroundFitnessService.class);
//        startForegroundService(foregroundServiceIntent);

        // Bind to the service
        Intent boundServiceIntent = new Intent(this, BoundFitnessService.class);
        bindService(boundServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void toggleTracking() {
        if (isServiceBound && fitnessService != null) {
            if (!fitnessService.isTrackingActive()) {
                fitnessService.startStepTracking();
                startTrackingButton.setText("🛑 Stop Tracking");
                Toast.makeText(this, "Fitness tracking started!", Toast.LENGTH_SHORT).show();
            } else {
                fitnessService.stopStepTracking();
                startTrackingButton.setText("🚀 Start Tracking");
                Toast.makeText(this, "Fitness tracking stopped.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateFitnessMetrics() {
        if (isServiceBound && fitnessService != null) {
            int steps = fitnessService.getCurrentSteps();
            double calories = fitnessService.getCaloriesBurned();
            int points = fitnessService.getPointsEarned();

            stepCountText.setText(String.format("%d steps", steps));
            caloriesText.setText(String.format("%.1f calories", calories));
            pointsText.setText(String.format("%d points", points));

            // Update progress bar
            stepProgressBar.setProgress(steps);

            // Sync with WearOS
            wearOSSync.syncFitnessData(steps, calories, points);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFitnessMetrics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unregister broadcast receiver
        if (stepMotivationReceiver != null) {
            unregisterReceiver(stepMotivationReceiver);
        }

        if (isServiceBound) {
            unbindService(serviceConnection);
        }
    }

    // Check and request activity recognition permission
    private boolean checkAndRequestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, 
                    Manifest.permission.ACTIVITY_RECOGNITION) 
                    != PackageManager.PERMISSION_GRANTED) {
                
                // Request the permission
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
                
                return false;
            }
        }
        return true;
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, 
                                           @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start tracking
                toggleTracking();
            } else {
                // Permission denied
                Toast.makeText(this, 
                    "Activity recognition permission is required to track steps", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    // Implement StepUpdateListener
    @Override
    public void onStepUpdate(int steps, double calories, int points) {
        // Update UI elements
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Log detailed step information


                stepCountText.setText(String.format("%d steps", steps));
                caloriesText.setText(String.format("%.1f calories", calories));
                pointsText.setText(String.format("%d points", points));

                // Update progress bar
                stepProgressBar.setProgress(steps);

                // Sync with WearOS
                wearOSSync.syncFitnessData(steps, calories, points);

                // Optional: Show toast for milestone steps
                if (steps > 0 && steps % 100 == 0) {
                    Toast.makeText(MainActivity.this, 
                        String.format("Great job! You've reached %d steps!", steps), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void generateWorkoutSuggestion(int steps, double calories, int points) {
        String prompt = String.format(
            "Based on the following fitness data, suggest a personalized workout:\n" +
            "Steps taken: %d\n" +
            "Calories burned: %.1f\n" +
            "Fitness points: %d\n\n" +
            "Please provide a specific, actionable workout suggestion that would complement these metrics. " +
            "Include duration, intensity, and specific exercises if applicable.",
            steps, calories, points
        );

        GeminiRequest request = new GeminiRequest(
            Collections.singletonList(
                new GeminiRequest.Content(
                    Collections.singletonList(
                        new GeminiRequest.Part(prompt)
                    )
                )
            )
        );

        GeminiClient.getInstance().getService().generateContent(BuildConfig.GEMINI_API_KEY, request)
            .enqueue(new Callback<GeminiResponse>() {
                @Override
                public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                    getWorkoutSuggestionButton.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String suggestion = response.body().getCandidates().get(0)
                                .getContent().getParts().get(0).getText();
                            
                            // Update UI with suggestion
                            motivationText.setText(suggestion);
                        } catch (Exception e) {
                            motivationText.setText("Error processing workout suggestion");
                            Toast.makeText(MainActivity.this, 
                                "Failed to get workout suggestion: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        motivationText.setText("Unable to get workout suggestion");
                        Toast.makeText(MainActivity.this, 
                            "API Error: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GeminiResponse> call, Throwable t) {
                    getWorkoutSuggestionButton.setEnabled(true);
                    motivationText.setText("Failed to get workout suggestion");
                    Toast.makeText(MainActivity.this, 
                        "Network error: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    // Add a new method for reset functionality
    private void resetFitnessTracking() {
        if (isServiceBound && fitnessService != null) {
            // Reset without auto-restart
            fitnessService.resetTracking(false);
            
            // Reset UI elements
            stepCountText.setText("0 steps");
            caloriesText.setText("0.0 calories");
            pointsText.setText("0 points");
            motivationText.setText("Fitness tracking reset. Tap 'Start Tracking' to begin.");
            
            // Reset progress bar
            stepProgressBar.setProgress(0);
            
            // Update start tracking button
            startTrackingButton.setText("🚀 Start Tracking");
            
            // Show reset confirmation
            Toast.makeText(this, "Fitness tracking has been reset. Tracking is stopped.", Toast.LENGTH_LONG).show();
        }
    }
} 