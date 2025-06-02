package com.example.fitnesstracker;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class WearOSSync {
    private static final String TAG = "WearOSSync";
    private static final String FITNESS_DATA_PATH = "/fitness_data";

    private Context context;
    private DataClient dataClient;

    public WearOSSync(Context context) {
        this.context = context;
        this.dataClient = Wearable.getDataClient(context);
    }

    public void syncFitnessData(int steps, double calories, int points) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(FITNESS_DATA_PATH);
        putDataMapRequest.getDataMap().putInt("steps", steps);
        putDataMapRequest.getDataMap().putDouble("calories", calories);
        putDataMapRequest.getDataMap().putInt("points", points);
        putDataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = dataClient.putDataItem(request);
        dataItemTask.addOnSuccessListener(dataItem -> 
            Log.d(TAG, "Fitness data synced to WearOS")
        ).addOnFailureListener(e -> 
            Log.e(TAG, "Failed to sync fitness data", e)
        );
    }

    public void requestFitnessDataUpdate() {
        // In a real implementation, this would request data from the phone
        Log.d(TAG, "Requesting fitness data update from phone");
    }
} 