package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime


interface ICommentItem : IBaseItem, ICommentableItem {
    val parent: ItemId?
    val text: String?
}

data class CommentItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: String?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val kids: List<ItemId>,
    override val parent: ItemId?,
    override val text : String?
) : ICommentItem {
    override val type: ItemType = ItemType.COMMENT

}