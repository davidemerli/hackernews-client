package it.devddk.hackernewsclient.domain.interaction.feedback

import it.devddk.hackernewsclient.domain.model.feedback.Feedback
import it.devddk.hackernewsclient.domain.repository.FeedbackRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface PostFeedbackUseCase {
    suspend operator fun invoke(feedback: Feedback) : Result<Unit>
}

class PostFeedbackUseCaseImpl : PostFeedbackUseCase, KoinComponent {

    private val feedbackRepository : FeedbackRepository by inject()

    override suspend operator fun invoke(feedback: Feedback) : Result<Unit> {
        return feedbackRepository.sendFeedback(feedback)
    }
}