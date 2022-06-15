package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.feedback.Feedback

interface FeedbackRepository {
    suspend fun sendFeedback(feedback: Feedback) : Result<Unit>

}