package it.devddk.hackernewsclient.domain.model


import it.devddk.hackernewsclient.domain.model.items.BaseItem
import java.time.*

data class User(
    val id: String,
    val createdGMT: LocalDateTime,
    val karma: Int,
    val delay: Period? = null,
    val about: String? = null,
    val submitted: List<BaseItem>,
)
