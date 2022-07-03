package it.devddk.hackernewsclient.data.networking.model

import androidx.annotation.Keep
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.domain.model.feedback.Feedback

@Keep
data class FeedbackEntity(val id: Int? = null, val feedback: String? = null, val impact: Int? = null) : DomainMapper<Feedback> {
    override fun mapToDomainModel(): Feedback {
        return Feedback(id, feedback ?: "", impact = 0)
    }

}