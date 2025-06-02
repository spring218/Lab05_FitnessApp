package com.example.fitnesstracker;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundFitnessService extends Service {
    private static final String TAG = "BackgroundFitnessService";
    private ScheduledExecutorService executorService;
    private FitnessDatabase database;
    private int currentSteps = 0;
    private Random random = new Random();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Background service created");
        database = FitnessDatabase.getInstance(getApplicationContext());
        
        // Initialize step tracking executor
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(stepTrackingRunnable, 0, 5, TimeUnit.SECONDS);
    }

    private Runnable stepTrackingRunnable = new Runnable() {
        @Override
        public void run() {
            // Simulate step counting
            int simulatedSteps = random.nextInt(50);
            currentSteps += simulatedSteps;

            // Calculate calories (0.04 calories per step)
            double calories = currentSteps * 0.04;

            // Calculate points (1 point per 10 steps)
            int points = currentSteps / 10;

            // Log data
            Log.d(TAG, "Steps: " + currentSteps + ", Calories: " + calories + ", Points: " + points);

            // Save to database every 100 steps
            if (currentSteps % 100 == 0) {
                saveToDatabase(currentSteps, calories, points);
            }
        }
    };

    private void saveToDatabase(int steps, double calories, int points) {
        new Thread(() -> {
            FitnessData fitnessData = new FitnessData(steps, calories, points, System.currentTimeMillis());
            database.fitnessDao().insert(fitnessData);
            Log.d(TAG, "Data saved to database");
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Background service started");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        Log.d(TAG, "Background service destroyed");
    }
} 