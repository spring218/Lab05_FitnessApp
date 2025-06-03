package com.example.fitnesstracker.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeminiRequest {
    @SerializedName("contents")
    private List<Content> contents;

    public GeminiRequest(List<Content> contents) {
        this.contents = contents;
    }

    public static class Content {
        @SerializedName("parts")
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        public Part(String text) {
            this.text = text;
        }
    }
} 