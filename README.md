# 🏃‍♂️ Fitness Quest - Android Fitness Tracker

## 📱 Overview
Fitness Quest is a comprehensive Android fitness tracking application designed for Mobile Development Lab 5. The app provides real-time step tracking, calorie calculation, points system, and AI-powered motivational coaching.

## ✨ Features
- 📊 Real-time Step Tracking
- 🔥 Automated Calorie Calculation
- ⭐ Gamified Points System
- 🏆 Progressive Achievement Badges
- 🤖 AI-Powered Motivational Coaching
- ⌚ WearOS Sync Capabilities

## 🛠️ Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 21+
- Java 8+
- Gemini API Key (for AI features)

## 📦 Dependencies
Add the following to your `app/build.gradle`:
```groovy
dependencies {
    // Room Database
    implementation "androidx.room:room-runtime:2.4.3"
    annotationProcessor "androidx.room:room-compiler:2.4.3"
    
    // Retrofit for API calls
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    
    // Material Design
    implementation "com.google.android.material:material:1.6.1"
    
    // Gemini AI
    implementation "com.google.ai.client.generativeai:generativeai:0.1.0"
    
    // WearOS
    implementation "com.google.android.gms:play-services-wearable:18.0.0"
}
```

## 🔧 Configuration
1. Replace `YOUR_GEMINI_API_KEY` in `GeminiAPIClient.java` with your actual Gemini API key
2. Ensure all required permissions are added to `AndroidManifest.xml`

## 📋 Permissions
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

## 🚀 Getting Started
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on an Android device or emulator (API 21+)

## 🧪 Testing
- Verify service start/stop functionality
- Check step counting and calorie calculations
- Test database persistence
- Validate AI motivation feature

## 🎯 Future Enhancements
- Real sensor integration
- Social leaderboards
- GPS activity tracking
- Advanced analytics

## 📚 Course Information
- **Course**: Mobile Development (Lab 5)
- **Instructor**: Tran Vinh Khiem
- **Date**: March 1st, 2022
- **Team**: Smart Software System Team

## 📝 License
[Insert your license here]

## 🙏 Acknowledgments
- Android Open Source Project
- Google AI Generative API
- Material Design Guidelines 