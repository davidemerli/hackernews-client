package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.lang.Exception
import java.time.LocalDateTime


interface ICommentableItem : IBaseItem {
    val kids : Map<ItemId, ICommentItem?>
}

data class CommentableItem(
    override val id: Int,
    override val type: ItemType,
    override val deleted: Boolean,
    override val by: Expandable<String, User>?,
    override val time: LocalDateTime?,
    override val dead: Boolean,
    override val kids: Map<ItemId, ICommentItem?>
) : ICommentableItem