package com.example.fitnesstracker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FitnessData.class}, version = 1, exportSchema = false)
public abstract class FitnessDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "fitness_tracker_db";
    private static FitnessDatabase instance;

    public abstract FitnessDao fitnessDao();

    public static synchronized FitnessDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    FitnessDatabase.class,
                    DATABASE_NAME
            ).fallbackToDestructiveMigration()
             .build();
        }
        return instance;
    }
} 