package com.youtube.auto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.auto.auth.AuthState
import com.youtube.auto.auth.GoogleAuthManager
import com.youtube.auto.auth.TokenRepository
import com.youtube.auto.data.model.SearchResult
import com.youtube.auto.data.model.Subscription
import com.youtube.auto.data.model.Video
import com.youtube.auto.data.repository.HistoryRepository
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: YouTubeRepository,
    private val historyRepository: HistoryRepository,
    private val authManager: GoogleAuthManager,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authManager.authState

    private val _trendingVideos = MutableStateFlow<Result<List<Video>>>(Result.Loading)
    val trendingVideos: StateFlow<Result<List<Video>>> = _trendingVideos.asStateFlow()

    private val _searchResults = MutableStateFlow<Result<SearchResult>?>(null)
    val searchResults: StateFlow<Result<SearchResult>?> = _searchResults.asStateFlow()

    private val _subscriptions = MutableStateFlow<Result<List<Subscription>>>(Result.Loading)
    val subscriptions: StateFlow<Result<List<Subscription>>> = _subscriptions.asStateFlow()

    val history: Flow<List<Video>> = historyRepository.getHistory()

    private var searchJob: Job? = null

    init {
        authManager.checkExistingSignIn()
    }

    fun loadTrending() {
        viewModelScope.launch {
            _trendingVideos.value = Result.Loading
            when (val result = repository.getTrendingVideos()) {
                is Result.Success -> _trendingVideos.value = Result.Success(result.data.videos)
                is Result.Error -> _trendingVideos.value = Result.Error(result.message, result.cause)
                is Result.Loading -> {}
            }
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchResults.value = Result.Loading
            _searchResults.value = repository.searchVideos(query)
        }
    }

    fun loadSubscriptions() {
        viewModelScope.launch {
            _subscriptions.value = Result.Loading
            _subscriptions.value = repository.getSubscriptions()
        }
    }

    fun addToHistory(video: Video) {
        viewModelScope.launch {
            historyRepository.addToHistory(video)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // Sign out from Google first, then clear local data
            authManager.signOut()
            tokenRepository.clearTokens()
            repository.cleanExpiredCache()
        }
    }
}
