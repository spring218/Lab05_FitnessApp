package com.example.fitnesstracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ForegroundFitnessService extends Service {
    private static final String TAG = "ForegroundFitnessService";
    private static final String CHANNEL_ID = "FitnessTrackerChannel";
    private static final int NOTIFICATION_ID = 1;

    private ScheduledExecutorService executorService;
    private int currentSteps = 0;
    private Random random = new Random();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        // Initialize step tracking executor
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(updateNotificationRunnable, 0, 10, TimeUnit.SECONDS);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Fitness Tracker",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Continuous fitness tracking");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        // Create an intent that will open the MainActivity when the notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE
        );

        // Motivational quotes array
        String[] motivationalQuotes = {
            "Every step counts! Keep moving!",
            "You're doing great! Stay active!",
            "Small steps lead to big changes!",
            "Your fitness journey starts now!"
        };

        String motivationalQuote = motivationalQuotes[random.nextInt(motivationalQuotes.length)];

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fitness Tracker")
                .setContentText(String.format("%d steps | %s", currentSteps, motivationalQuote))
                .setSmallIcon(R.drawable.ic_fitness_icon)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            // Simulate step counting
            int simulatedSteps = random.nextInt(50);
            currentSteps += simulatedSteps;

            // Update notification
            NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Foreground service started");
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
        Log.d(TAG, "Foreground service destroyed");
    }
} 