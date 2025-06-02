package com.example.fitnesstracker;

import android.app.Application;
import android.util.Log;

public class FitnessApplication extends Application {
    private static final String TAG = "FitnessApplication";
    private FitnessDatabase database;

    private WearOSSync wearOSSync;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Fitness Tracker Application initialized");

        // Initialize global components
        initializeDatabase();

        initializeWearOSSync();
    }

    private void initializeDatabase() {
        database = FitnessDatabase.getInstance(getApplicationContext());
        Log.d(TAG, "Room Database initialized");
    }



    private void initializeWearOSSync() {
        wearOSSync = new WearOSSync(getApplicationContext());
        Log.d(TAG, "WearOS Sync initialized");
    }

    // Getter methods for global components
    public FitnessDatabase getDatabase() {
        return database;
    }



    public WearOSSync getWearOSSync() {
        return wearOSSync;
    }
} 