package com.example.fitnesstracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FitnessDao {
    @Insert
    void insert(FitnessData fitnessData);

    @Query("SELECT * FROM fitness_data ORDER BY timestamp DESC")
    List<FitnessData> getAllData();

    @Query("SELECT SUM(steps) FROM fitness_data")
    int getTotalSteps();

    @Query("SELECT SUM(calories) FROM fitness_data")
    double getTotalCalories();

    @Query("SELECT SUM(points) FROM fitness_data")
    int getTotalPoints();

    @Query("SELECT * FROM fitness_data WHERE timestamp BETWEEN :startTime AND :endTime")
    List<FitnessData> getDataBetweenTimestamps(long startTime, long endTime);
} 