package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import it.devddk.hackernewsclient.domain.interaction.feedback.PostFeedbackUseCase
import it.devddk.hackernewsclient.domain.model.feedback.Feedback
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FeedbackViewModel : ViewModel(), KoinComponent {

    val postFeedback : PostFeedbackUseCase by inject()


    suspend fun submitFeedback(feedback : Feedback) : Result<Unit> {
        return postFeedback(feedback)
    }
}