package it.devddk.hackernewsclient.domain.model


import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.*

data class User(
    val id: String,
    val created: LocalDateTime? = null,
    val karma: Int? = null,
    val about: String? = null,
    val submitted: List<ItemId> = emptyList(),
)
