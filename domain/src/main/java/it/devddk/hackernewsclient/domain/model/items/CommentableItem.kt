package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime


interface ICommentableItem : IBaseItem {
    val kids: List<ItemId>
}

data class CommentableItem(
    override val id: Int,
    override val type: ItemType,
    override val deleted: Boolean,
    override val by: String?,
    override val time: LocalDateTime?,
    override val dead: Boolean,
    override val kids: List<ItemId>
) : ICommentableItem