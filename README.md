# YouTube Android Auto

A complete YouTube client for Android Auto with Google Sign-In, video browsing, search, and playback.

## Features

- **Trending Videos** - Browse currently popular videos
- **Search** - Search for any YouTube content
- **Subscriptions** - View your subscribed channels (requires sign-in)
- **Playlists** - Browse your playlists (requires sign-in)
- **Watch History** - Track recently watched videos
- **Settings** - Video quality, autoplay, data saver preferences
- **Android Auto** - Full Car App Library integration with optimized UI

## Architecture

- **MVVM** with Kotlin Coroutines and StateFlow
- **Hilt** for dependency injection
- **Retrofit** for YouTube Data API v3 calls
- **Room** for local caching and history
- **Media3 (ExoPlayer)** for video playback
- **AndroidX Car App Library** for Android Auto screens

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Google Cloud Console project with YouTube Data API v3 enabled

### API Keys

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable **YouTube Data API v3**
4. Create an **API Key** for YouTube Data API
5. Create an **OAuth 2.0 Client ID** (Android type)
6. Copy `local.properties.template` to `local.properties`
7. Fill in your API keys:

```properties
YOUTUBE_API_KEY=your_youtube_api_key
GOOGLE_CLIENT_ID=your_google_client_id
```

### Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Run tests
./gradlew testDebugUnitTest
```

### Testing with Android Auto

1. Install the app on your phone
2. Install [Android Auto Desktop Head Unit (DHU)](https://developer.android.com/training/cars/testing)
3. Connect phone via USB
4. Launch DHU to see the app on the car screen

## Project Structure

```
app/src/main/java/com/youtube/auto/
├── auth/                    # Google Sign-In & token management
│   ├── GoogleAuthManager.kt
│   └── TokenRepository.kt
├── car/                     # Android Auto screens
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── TrendingScreen.kt
│   │   ├── SearchScreen.kt
│   │   ├── VideoPlayerScreen.kt
│   │   ├── SubscriptionsScreen.kt
│   │   ├── PlaylistScreen.kt
│   │   ├── HistoryScreen.kt
│   │   └── SettingsScreen.kt
│   └── service/
│       └── YouTubeCarAppService.kt
├── data/
│   ├── api/
│   │   └── YouTubeApiService.kt
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   ├── model/
│   │   ├── Video.kt
│   │   ├── Channel.kt
│   │   ├── Playlist.kt
│   │   └── ...
│   └── repository/
│       ├── YouTubeRepository.kt
│       └── HistoryRepository.kt
├── di/                      # Hilt modules
│   ├── AppModule.kt
│   └── GsonModule.kt
├── domain/usecase/          # Business logic
├── player/                  # Media playback
│   ├── PlaybackService.kt
│   └── VideoPlayerManager.kt
├── settings/
│   └── SettingsManager.kt
├── ui/
│   ├── activity/
│   │   ├── AuthActivity.kt
│   │   ├── MainActivity.kt
│   │   └── VideoPlayerActivity.kt
│   └── viewmodel/
│       └── MainViewModel.kt
└── util/
    ├── Constants.kt
    ├── ErrorHandler.kt
    ├── NetworkUtils.kt
    └── Result.kt
```

## CI/CD

GitHub Actions workflow runs on every push and PR to `main`:
- Builds debug APK
- Runs unit tests
- Runs lint checks
- Uploads APK as artifact

## License

MIT License - see [LICENSE](LICENSE) file.
