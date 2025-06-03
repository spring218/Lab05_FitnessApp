package com.example.fitnesstracker.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiService {
    @POST("v1beta/models/gemini-pro:generateContent")
    Call<GeminiResponse> generateContent(
        @Query("key") String apiKey,
        @Body GeminiRequest request
    );
} 