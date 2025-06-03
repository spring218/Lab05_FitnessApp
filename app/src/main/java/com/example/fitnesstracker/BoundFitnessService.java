package com.example.fitnesstracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.fitnesstracker.api.GeminiClient;
import com.example.fitnesstracker.api.GeminiRequest;
import com.example.fitnesstracker.api.GeminiResponse;

import java.util.Collections;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoundFitnessService extends Service implements SensorEventListener {
    private static final String TAG = "BoundFitnessService";
    private final IBinder binder = new LocalBinder();
    private int currentSteps = 0;
    private double caloriesBurned = 0;
    private int pointsEarned = 0;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isTracking = false;
    private Handler simulatorHandler;
    private Runnable simulatorRunnable;

    // Step update listener
    private StepUpdateListener stepUpdateListener;

    public interface StepUpdateListener {
        void onStepUpdate(int steps, double calories, int points);
    }

    public class LocalBinder extends Binder {
        BoundFitnessService getService() {
            return BoundFitnessService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        simulatorHandler = new Handler(Looper.getMainLooper());

        // Log step sensor availability
        if (stepSensor != null) {
            Log.i(TAG, "Step sensor available: " + stepSensor.getName());
            Log.i(TAG, "Step sensor vendor: " + stepSensor.getVendor());
            Log.i(TAG, "Step sensor version: " + stepSensor.getVersion());
        } else {
            Log.w(TAG, "No step sensor found. Will use simulator mode.");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setStepUpdateListener(StepUpdateListener listener) {
        this.stepUpdateListener = listener;
    }

    public void startStepTracking() {
        // Reset metrics before starting
        currentSteps = 0;
        caloriesBurned = 0;
        pointsEarned = 0;

        if (stepSensor != null) {
            // Real step sensor available
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            isTracking = true;
            Log.d(TAG, "Step tracking started with real sensor");
        } else {
            // Simulator mode for devices without step sensor
            startStepSimulation();
        }
    }

    private void startStepSimulation() {
        isTracking = true;
        simulatorRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    // More intelligent step simulation
                    int simulatedSteps;
                    if (currentSteps < 500) {
                        // Beginner level: Slower step increase
                        simulatedSteps = new Random().nextInt(3) + 1;
                    } else if (currentSteps < 2000) {
                        // Intermediate level: Moderate step increase
                        simulatedSteps = new Random().nextInt(5) + 3;
                    } else {
                        // Advanced level: Higher step increase
                        simulatedSteps = new Random().nextInt(7) + 5;
                    }

                    currentSteps += simulatedSteps;
                    caloriesBurned = currentSteps * 0.04;
                    pointsEarned = currentSteps / 10;

                    Log.d(TAG, String.format(
                        "Intelligent Simulator - Steps: %d, Calories: %.2f, Points: %d", 
                        currentSteps, caloriesBurned, pointsEarned
                    ));

                    // Notify listener on main thread
                    if (stepUpdateListener != null) {
                        stepUpdateListener.onStepUpdate(currentSteps, caloriesBurned, pointsEarned);
                    }

                    // Generate motivation at different milestones
                    if (currentSteps % 500 == 0) {
                        generateStepMotivation();
                    }

                    // Schedule next step simulation with slight variation
                    simulatorHandler.postDelayed(this, 2000 + new Random().nextInt(1000));
                }
            }
        };
        simulatorHandler.post(simulatorRunnable);
        Log.d(TAG, "Intelligent step tracking started in simulator mode");
    }

    public void stopStepTracking() {
        if (stepSensor != null) {
            sensorManager.unregisterListener(this);
        } else {
            // Stop simulator
            if (simulatorHandler != null && simulatorRunnable != null) {
                simulatorHandler.removeCallbacks(simulatorRunnable);
            }
        }
        isTracking = false;
        Log.d(TAG, "Step tracking stopped");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR && isTracking) {
            currentSteps++;
            caloriesBurned = currentSteps * 0.04;
            pointsEarned = currentSteps / 10;

            // Notify listener on main thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (stepUpdateListener != null) {
                        stepUpdateListener.onStepUpdate(currentSteps, caloriesBurned, pointsEarned);
                    }
                }
            });

            // Optional: Generate motivation when milestone reached
            if (currentSteps % 1000 == 0) {
                generateStepMotivation();
            }

            Log.d(TAG, "Steps: " + currentSteps);
        }
    }

    private void generateStepMotivation() {
        String prompt = String.format(
            "I've reached %d steps today. Analyze my current fitness progress and provide:" +
            "\n1. A personalized step count goal for the rest of the day" +
            "\n2. A targeted workout suggestion based on my current activity level" +
            "\n3. Motivational advice to help me stay active" +
            "\nProvide the response in a structured, encouraging format.",
            currentSteps
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
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String motivation = response.body().getCandidates().get(0)
                                .getContent().getParts().get(0).getText();
                            
                            // Intelligent step goal adjustment
                            int recommendedSteps = intelligentStepGoalAdjustment(motivation);
                            
                            Log.d(TAG, "Gemini Motivation: " + motivation);
                            Log.d(TAG, "Recommended Daily Steps: " + recommendedSteps);
                            
                            // Broadcast motivation and step goal
                            broadcastStepMotivation(motivation, recommendedSteps);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing Gemini response", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<GeminiResponse> call, Throwable t) {
                    Log.e(TAG, "Failed to get motivation from Gemini", t);
                }
            });
    }

    private int intelligentStepGoalAdjustment(String geminiResponse) {
        // Extract step goal from Gemini's response using simple parsing
        try {
            // Look for numeric step goal in the response
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)[,]?\\s*steps?");
            java.util.regex.Matcher matcher = pattern.matcher(geminiResponse);
            
            if (matcher.find()) {
                int recommendedSteps = Integer.parseInt(matcher.group(1));
                return Math.max(recommendedSteps, currentSteps + 1000); // Ensure progressive goal
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not parse step goal from response", e);
        }
        
        // Fallback to default progressive goal
        return currentSteps + 1000;
    }

    private void broadcastStepMotivation(String motivation, int recommendedSteps) {
        Intent motivationIntent = new Intent("com.example.fitnesstracker.STEP_MOTIVATION");
        motivationIntent.putExtra("motivation", motivation);
        motivationIntent.putExtra("recommendedSteps", recommendedSteps);
        sendBroadcast(motivationIntent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step detection
    }

    public int getCurrentSteps() {
        return currentSteps;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void resetTracking() {
        resetTracking(true);
    }

    public void resetTracking(boolean autoRestart) {
        // Reset all tracking metrics
        currentSteps = 0;
        caloriesBurned = 0;
        pointsEarned = 0;

        // Stop current tracking
        stopStepTracking();

        // Optionally restart tracking
        if (autoRestart) {
            startStepTracking();
            Log.d(TAG, "Fitness tracking reset and auto-restarted");
        } else {
            Log.d(TAG, "Fitness tracking reset without auto-restart");
        }

        // Notify listener about the reset
        if (stepUpdateListener != null) {
            stepUpdateListener.onStepUpdate(currentSteps, caloriesBurned, pointsEarned);
        }
    }

    // Add method to check if tracking is active
    public boolean isTrackingActive() {
        return isTracking;
    }
} 