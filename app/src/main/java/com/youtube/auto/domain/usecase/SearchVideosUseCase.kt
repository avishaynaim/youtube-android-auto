package com.youtube.auto.domain.usecase

import com.youtube.auto.data.model.SearchResult
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import javax.inject.Inject

class SearchVideosUseCase @Inject constructor(
    private val repository: YouTubeRepository
) {
    suspend operator fun invoke(query: String, pageToken: String? = null): Result<SearchResult> {
        if (query.isBlank()) return Result.Error("Search query cannot be empty")
        return repository.searchVideos(query.trim(), pageToken)
    }
}
