package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime

interface IPollItem : ICommentableItem {
    val title: String?
    val score: Int?
    val descendants: Int?
    val parts: List<ItemId>
}

data class PollItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: String?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val title: String?,
    override val score: Int?,
    override val descendants: Int?,
    override val kids: List<ItemId>,
    override val parts: List<ItemId>,
    ) : IPollItem {
    override val type: ItemType = ItemType.POLL
}