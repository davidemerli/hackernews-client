package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class CommentItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: User,
    override val time: LocalDateTime,
    override val dead: Boolean = false,
    override val kids: List<BaseItem>? = null,
    override val parent: BaseItem
) : BaseItem, ChildItem {
    override val type: ItemType = ItemType.COMMENT

}