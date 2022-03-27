package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class AskHNItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: User?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val kids: List<CommentItem>,
    override val title: String,
    override val score: Int,
    override val descendants: Int,
    val text : String
) : BaseItem, MainItem, CommentableItem {
    override val type: ItemType = ItemType.STORY
}