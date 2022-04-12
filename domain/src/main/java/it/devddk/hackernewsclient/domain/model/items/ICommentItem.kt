package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime


interface ICommentItem : IBaseItem, ICommentableItem {
    val parent: Expandable<ItemId, Item>?
    val text: String?
}

data class CommentItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: Expandable<String, User>?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val kids: Map<ItemId, ICommentItem?>,
    override val parent: Expandable<ItemId, Item>?,
    override val text : String?
) : ICommentItem {
    override val type: ItemType = ItemType.COMMENT

}