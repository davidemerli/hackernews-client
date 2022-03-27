package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class PollItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: User?,
    override val time: LocalDateTime,
    override val dead: Boolean = false,
    override val title: String,
    override val score: Int,
    override val descendants: Int,
    override val kids: List<CommentItem>,
    val parts : List<PollOptItem>,
    ) : BaseItem, CommentableItem, MainItem {
    override val type: ItemType = ItemType.POLL
}