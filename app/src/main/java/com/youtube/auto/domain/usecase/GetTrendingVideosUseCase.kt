package com.youtube.auto.domain.usecase

import com.youtube.auto.data.model.SearchResult
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import javax.inject.Inject

class GetTrendingVideosUseCase @Inject constructor(
    private val repository: YouTubeRepository
) {
    suspend operator fun invoke(pageToken: String? = null): Result<SearchResult> {
        return repository.getTrendingVideos(pageToken)
    }
}
