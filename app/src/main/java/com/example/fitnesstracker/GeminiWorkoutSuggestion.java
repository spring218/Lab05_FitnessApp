package com.example.fitnesstracker;

import android.content.Context;
import android.util.Log;

import com.example.fitnesstracker.api.GeminiClient;
import com.example.fitnesstracker.api.GeminiRequest;
import com.example.fitnesstracker.api.GeminiResponse;

import java.util.Collections;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeminiWorkoutSuggestion {
    private static final String TAG = "GeminiWorkoutSuggestion";
    private final String apiKey;

    public GeminiWorkoutSuggestion(Context context) {
        // Use the API key from BuildConfig
        this.apiKey = BuildConfig.GEMINI_API_KEY;
    }

    public interface WorkoutSuggestionCallback {
        void onSuggestionReceived(String suggestion);
        void onError(String error);
    }

    public void getWorkoutSuggestion(int steps, int calories, int points, WorkoutSuggestionCallback callback) {
        // Check if API key is available or use simulator
        if (apiKey == null || apiKey.isEmpty()) {
            String simulatorSuggestion = generateSimulatorSuggestion(steps, calories, points);
            callback.onSuggestionReceived(simulatorSuggestion);
            return;
        }

        // Determine fitness level and suggest appropriate workout
        String fitnessLevel = determineFitnessLevel(steps, calories, points);
        
        String prompt = String.format(Locale.getDefault(),
            "You are a professional fitness coach providing personalized workout advice. " +
            "Analyze the following fitness metrics:\n" +
            "- Steps taken: %d\n" +
            "- Calories burned: %d\n" +
            "- Fitness points: %d\n" +
            "- Fitness Level: %s\n\n" +
            "Based on these metrics, provide a motivational and tailored workout suggestion. " +
            "Consider the user's current activity level and suggest a workout that will:\n" +
            "1. Gradually increase their fitness\n" +
            "2. Be engaging and achievable\n" +
            "3. Target overall fitness improvement\n\n" +
            "Format your response as a friendly, encouraging fitness coach. " +
            "Include specific exercises, duration, and motivational tips.",
            steps, calories, points, fitnessLevel
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

        GeminiClient.getInstance().getService().generateContent(apiKey, request)
            .enqueue(new Callback<GeminiResponse>() {
                @Override
                public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String suggestion = response.body().getCandidates().get(0)
                                .getContent().getParts().get(0).getText();
                            
                            // Call the callback with the suggestion
                            callback.onSuggestionReceived(suggestion);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing suggestion", e);
                            callback.onError("Failed to process workout suggestion");
                        }
                    } else {
                        Log.e(TAG, "API response unsuccessful: " + response.code());
                        callback.onError("Unable to get workout suggestion");
                    }
                }

                @Override
                public void onFailure(Call<GeminiResponse> call, Throwable t) {
                    Log.e(TAG, "Network error", t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
    }

    private String determineFitnessLevel(int steps, int calories, int points) {
        // Create a simple fitness level assessment
        if (steps < 50 && calories < 5 && points < 5) {
            return "Beginner";
        } else if (steps < 500 && calories < 20 && points < 20) {
            return "Novice";
        } else if (steps < 2000 && calories < 50 && points < 50) {
            return "Intermediate";
        } else if (steps < 5000 && calories < 100 && points < 100) {
            return "Active";
        } else {
            return "Advanced";
        }
    }

    // Fallback method for offline or no-network scenarios
    public String generateOfflineSuggestion(int steps, int calories, int points) {
        String fitnessLevel = determineFitnessLevel(steps, calories, points);
        
        switch (fitnessLevel) {
            case "Beginner":
                return "🏋️ Beginner Fitness Boost: Start with a 10-minute walk, " +
                       "followed by 5 minutes of gentle stretching. Focus on building " +
                       "consistency and enjoying movement.";
            case "Novice":
                return "🚶‍♀️ Novice Fitness Plan: Take a 20-minute brisk walk, " +
                       "add 10 bodyweight squats and 10 push-ups. Listen to your body " +
                       "and gradually increase intensity.";
            case "Intermediate":
                return "🏃 Intermediate Workout: 30-minute mixed cardio - " +
                       "15 min jogging, 10 min strength training (lunges, push-ups, " +
                       "planks), 5 min cool-down. Challenge yourself!";
            case "Active":
                return "💪 Active Fitness Challenge: 45-minute high-intensity " +
                       "interval training (HIIT). Alternate between cardio bursts " +
                       "and strength exercises. Stay hydrated and push your limits!";
            case "Advanced":
                return "🔥 Advanced Fitness Regime: 60-minute comprehensive " +
                       "workout - 20 min running, 20 min weight training, 10 min " +
                       "core work, 10 min yoga/stretching. You're a fitness champion!";
            default:
                return "🌟 Keep moving! Every step counts towards your fitness journey.";
        }
    }

    // Simulator method for workout suggestions
    public String generateSimulatorSuggestion(int steps, int calories, int points) {
        // Categorize fitness level based on current metrics
        String fitnessLevel = determineFitnessLevel(steps, calories, points);
        
        // Predefined workout suggestions based on fitness level
        switch (fitnessLevel) {
            case "Beginner":
                return "🌱 Beginner Fitness Journey:\n" +
                       "- 10-minute gentle walk\n" +
                       "- 5 bodyweight squats\n" +
                       "- 5 wall push-ups\n" +
                       "Focus on form and consistency. You're just starting, and every step counts!";
            
            case "Novice":
                return "🚶‍♀️ Novice Fitness Boost:\n" +
                       "- 20-minute brisk walking\n" +
                       "- 10 lunges (each leg)\n" +
                       "- 10 modified push-ups\n" +
                       "- 30-second plank\n" +
                       "Building strength and endurance. Keep pushing!";
            
            case "Intermediate":
                return "🏋️ Intermediate Workout Challenge:\n" +
                       "- 30-minute mixed cardio\n" +
                       "  * 15 min jogging\n" +
                       "  * 10 min strength training\n" +
                       "- 15 bodyweight squats\n" +
                       "- 12 push-ups\n" +
                       "- 45-second plank\n" +
                       "You're making great progress. Stay consistent!";
            
            case "Active":
                return "💪 Active Fitness Intensity:\n" +
                       "- 45-minute High-Intensity Interval Training (HIIT)\n" +
                       "  * 5 min warm-up\n" +
                       "  * 30 min alternating:\n" +
                       "    - Burpees\n" +
                       "    - Mountain climbers\n" +
                       "    - Jump squats\n" +
                       "  * 10 min cool-down\n" +
                       "Push your limits. You're becoming a fitness champion!";
            
            case "Advanced":
                return "🔥 Advanced Fitness Mastery:\n" +
                       "- 60-minute Comprehensive Workout\n" +
                       "  * 20 min running or cycling\n" +
                       "  * 20 min weight training\n" +
                       "    - Deadlifts\n" +
                       "    - Weighted squats\n" +
                       "    - Pull-ups/Assisted pull-ups\n" +
                       "  * 10 min core workout\n" +
                       "  * 10 min yoga/stretching\n" +
                       "You're a fitness elite. Keep challenging yourself!";
            
            default:
                return "🌟 Personalized Fitness Motivation:\n" +
                       "Every step is a step towards your goals. " +
                       "Stay active, stay motivated, and believe in yourself!";
        }
    }
} 