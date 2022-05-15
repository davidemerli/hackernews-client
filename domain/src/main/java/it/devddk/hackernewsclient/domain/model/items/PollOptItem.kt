package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime

interface IPollOptItem : IBaseItem {
    val text: String?
    val poll: ItemId?
    val score: Int?
}

data class PollOptItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: String?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val text : String?,
    override val poll: ItemId?,
    override val score : Int?
) : IPollOptItem {
    override val type: ItemType = ItemType.POLL_OPT
}