package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime

interface IPollItem : ICommentableItem {
    val title: String?
    val score: Int?
    val descendants: Int?
    val parts: Map<ItemId, PollOptItem?>
}

data class PollItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: Expandable<String, User>?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val title: String?,
    override val score: Int?,
    override val descendants: Int?,
    override val kids: Map<ItemId, ICommentItem?>,
    override val parts : Map<ItemId, PollOptItem?>,
    ) : IPollItem {
    override val type: ItemType = ItemType.POLL
}