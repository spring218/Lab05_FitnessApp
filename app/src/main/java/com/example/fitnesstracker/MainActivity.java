package com.example.fitnesstracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    private BoundFitnessService fitnessService;
    private boolean isServiceBound = false;
    private boolean isTracking = false;

    private TextView stepCountText;
    private TextView caloriesText;
    private TextView pointsText;
    private TextView motivationText;
    private ProgressBar stepProgressBar;
    private Button startTrackingButton;
    private Button syncMotivationButton;


    private WearOSSync wearOSSync;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BoundFitnessService.LocalBinder binder = (BoundFitnessService.LocalBinder) service;
            fitnessService = binder.getService();
            isServiceBound = true;
            updateFitnessMetrics();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            fitnessService = null;
            isServiceBound = false;
        }
    };

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

        // Get global components
        FitnessApplication app = (FitnessApplication) getApplication();

        wearOSSync = app.getWearOSSync();

        // Start background and foreground services
        startBackgroundServices();

        // Set up button listeners
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTracking();
            }
        });

        syncMotivationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void startBackgroundServices() {
        Intent backgroundServiceIntent = new Intent(this, BackgroundFitnessService.class);
        startService(backgroundServiceIntent);

        Intent foregroundServiceIntent = new Intent(this, ForegroundFitnessService.class);
        startForegroundService(foregroundServiceIntent);

        // Bind to the service
        Intent boundServiceIntent = new Intent(this, BoundFitnessService.class);
        bindService(boundServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void toggleTracking() {
        isTracking = !isTracking;

        if (isTracking) {
            startTrackingButton.setText("🛑 Stop Tracking");
            Toast.makeText(this, "Fitness tracking started!", Toast.LENGTH_SHORT).show();
        } else {
            startTrackingButton.setText("🚀 Start Tracking");
            Toast.makeText(this, "Fitness tracking stopped.", Toast.LENGTH_SHORT).show();
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
        if (isServiceBound) {
            unbindService(serviceConnection);
        }
    }
} 