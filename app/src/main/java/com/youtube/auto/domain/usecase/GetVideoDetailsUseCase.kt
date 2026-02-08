package com.youtube.auto.domain.usecase

import com.youtube.auto.data.model.Video
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import javax.inject.Inject

class GetVideoDetailsUseCase @Inject constructor(
    private val repository: YouTubeRepository
) {
    suspend operator fun invoke(videoId: String): Result<Video> {
        if (videoId.isBlank()) return Result.Error("Video ID cannot be empty")
        return repository.getVideoDetails(videoId)
    }
}
