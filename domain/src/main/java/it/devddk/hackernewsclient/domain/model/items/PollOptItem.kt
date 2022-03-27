package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class PollOptItem(
    override val id: Int,

    override val deleted: Boolean = false,
    override val by: User?,
    override val time: LocalDateTime,
    override val dead: Boolean = false,
    val poll : PollItem,
    val score : Int
) : BaseItem{
    override val type: ItemType = ItemType.POLL_OPT
}