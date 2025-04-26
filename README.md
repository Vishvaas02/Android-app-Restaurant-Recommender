
## Overview
An Android application that leverages the Google Maps API and OpenAI’s ChatGPT to deliver personalized restaurant recommendations within a user-specified radius. Users set their preferences (cuisine, price level, dietary restrictions) and the app displays nearby options with AI-generated insights.

## Features
- **Interactive Map**: Displays restaurants on Google Maps based on your location and radius  
- **Preference Filters**: Cuisine type, price range, dietary needs (vegetarian, vegan, gluten-free)  
- **ChatGPT Integration**: Personalized tips, menu highlights, and sentiment-based ratings summaries  
- **Material Design UI**: Clean, responsive interfaces  
- **Offline Caching**: (Optional) Stores recent searches locally with Room  
- **Secure API Injection**: Keys injected via Gradle, never hard-coded in source

## Setup & Installation

Clone the repo and configure your keys:
```bash
git clone https://github.com/Vishvaas02/Android-app-Restaurant-Recommender.git
cd Android-app-Restaurant-Recommender
Add your API keys to ~/.gradle/gradle.properties:

ini
Copy
Edit
OPENAI_API_KEY=sk-your-new-openai-key
GOOGLE_API_KEY=AIzaYourGoogleMapsKey
Open in Android Studio, sync Gradle, and run on a device (min SDK 24).

Tech Stack
Kotlin & Java

Android SDK (API 24+)

Google Maps SDK

OpenAI Chat Completions API

Retrofit & OkHttp

Gradle (Kotlin DSL)

Room (for optional caching)

