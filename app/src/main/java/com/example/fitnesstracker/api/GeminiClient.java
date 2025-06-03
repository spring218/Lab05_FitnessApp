package com.example.fitnesstracker.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiClient {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static GeminiClient instance;
    private final GeminiService service;

    private GeminiClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        service = retrofit.create(GeminiService.class);
    }

    public static synchronized GeminiClient getInstance() {
        if (instance == null) {
            instance = new GeminiClient();
        }
        return instance;
    }

    public GeminiService getService() {
        return service;
    }
} 