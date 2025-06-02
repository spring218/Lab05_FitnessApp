package com.example.fitnesstracker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BoundFitnessService extends Service {
    private final IBinder binder = new LocalBinder();
    private int currentSteps = 0;
    private double caloriesBurned = 0;
    private int pointsEarned = 0;

    public class LocalBinder extends Binder {
        BoundFitnessService getService() {
            return BoundFitnessService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public int getCurrentSteps() {
        // In a real implementation, this would fetch from the background service or database
        return currentSteps;
    }

    public double getCaloriesBurned() {
        // Calculate calories based on current steps
        return currentSteps * 0.04;
    }

    public int getPointsEarned() {
        // Calculate points based on current steps
        return currentSteps / 10;
    }

    // Method to update fitness metrics from background service
    public void updateMetrics(int steps, double calories, int points) {
        this.currentSteps = steps;
        this.caloriesBurned = calories;
        this.pointsEarned = points;
    }
} 