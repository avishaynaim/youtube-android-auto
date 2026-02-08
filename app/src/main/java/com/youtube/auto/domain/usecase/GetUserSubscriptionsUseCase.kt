package com.youtube.auto.domain.usecase

import com.youtube.auto.data.model.Subscription
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import javax.inject.Inject

class GetUserSubscriptionsUseCase @Inject constructor(
    private val repository: YouTubeRepository
) {
    suspend operator fun invoke(pageToken: String? = null): Result<List<Subscription>> {
        return repository.getSubscriptions(pageToken)
    }
}
