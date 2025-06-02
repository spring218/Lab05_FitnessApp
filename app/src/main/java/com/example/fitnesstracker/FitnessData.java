package com.example.fitnesstracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fitness_data")
public class FitnessData {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int steps;
    private double calories;
    private int points;
    private long timestamp;

    public FitnessData(int steps, double calories, int points, long timestamp) {
        this.steps = steps;
        this.calories = calories;
        this.points = points;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
} 