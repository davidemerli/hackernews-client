package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class PollOptItem(
    override val id: Int,
    override val type: ItemType,
    override val deleted: Boolean,
    override val by: User?,
    override val time: LocalDateTime,
    override val dead: Boolean,
    override val kids: List<BaseItem>?,
    override val parent: BaseItem,
    val score : Int?
) : BaseItem, ChildItem {
}