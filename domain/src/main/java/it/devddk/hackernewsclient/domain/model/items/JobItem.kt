package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class JobItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: User,
    override val time: LocalDateTime,
    override val dead: Boolean = false,
    override val title: String,
    override val score: Int,
    val text : String,
    val url : String
) : BaseItem, MainItem {
    override val type = ItemType.JOB
    override val descendants: Int = 0
}