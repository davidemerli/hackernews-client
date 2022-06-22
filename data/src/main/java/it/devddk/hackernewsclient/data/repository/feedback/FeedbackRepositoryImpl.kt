package it.devddk.hackernewsclient.data.repository.feedback

import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.networking.model.FeedbackEntity
import it.devddk.hackernewsclient.domain.model.feedback.Feedback
import it.devddk.hackernewsclient.domain.repository.FeedbackRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber
import java.lang.Exception

class FeedbackRepositoryImpl : FeedbackRepository, KoinComponent{

    private val feedbackReference : DatabaseReference by inject(named("feedback"))

    override suspend fun sendFeedback(feedback: Feedback) : Result<Unit> {
        return try {
            val feedbackEntity = FeedbackEntity(feedback.id, feedback.feedback, feedback.impact)
            val newRef = feedbackReference.push()

            // we do not await setValue since if the user is not connected to the internet,
            // Firebase will just schedule the message and never return an error, and awaits until it is
            // actually sent. We just assume it at sent.
            newRef.setValue(feedbackEntity)

            Result.success(Unit)
        } catch (ex : Exception) {
            Timber.e(ex)
            Result.failure(ex)
        }
    }
}