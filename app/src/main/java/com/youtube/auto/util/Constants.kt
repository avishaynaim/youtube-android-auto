package com.youtube.auto.util

object Constants {
    const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
    const val YOUTUBE_THUMBNAIL_URL = "https://i.ytimg.com/vi/%s/hqdefault.jpg"
    const val YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v=%s"

    const val MAX_RESULTS_DEFAULT = 20
    const val MAX_RESULTS_SEARCH = 25
    const val MAX_RESULTS_TRENDING = 50

    const val CACHE_TTL_TRENDING_MS = 30 * 60 * 1000L  // 30 minutes
    const val CACHE_TTL_SEARCH_MS = 10 * 60 * 1000L    // 10 minutes
    const val CACHE_TTL_CHANNEL_MS = 60 * 60 * 1000L   // 1 hour
    const val CACHE_TTL_VIDEO_MS = 24 * 60 * 60 * 1000L // 24 hours

    const val VIDEO_ID_PATTERN = "^[a-zA-Z0-9_-]{1,20}$"
    const val CHANNEL_ID_PATTERN = "^UC[a-zA-Z0-9_-]{22}$"

    const val PREFS_NAME = "youtube_auto_prefs"
    const val PREF_VIDEO_QUALITY = "video_quality"
    const val PREF_AUTOPLAY = "autoplay"
    const val PREF_DATA_SAVER = "data_saver"

    const val DB_NAME = "youtube_auto_db"

    const val REGION_CODE = "US"
    const val DEFAULT_LANGUAGE = "en"

    object Quality {
        const val AUTO = "auto"
        const val LOW = "360p"
        const val MEDIUM = "720p"
        const val HIGH = "1080p"
    }

    object CarApp {
        const val MIN_TOUCH_TARGET_DP = 76
        const val MAX_LIST_ITEMS = 6
        const val MAX_GRID_ITEMS = 6
    }
}
