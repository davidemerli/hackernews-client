package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import java.time.LocalDateTime

interface IBaseItem{

    companion object {
        inline fun <reified T : IBaseItem> itemCast(item : IBaseItem) : T? {
            return item as? T
        }
    }

    val id: Int
    val type: ItemType
    val deleted: Boolean
    val by: Expandable<String, User>?
    val time: LocalDateTime?
    val dead: Boolean
}

data class BaseItem(
    override val id: Int,
    override val type: ItemType,
    override val deleted: Boolean,
    override val by: Expandable<String, User>?,
    override val time: LocalDateTime?,
    override val dead: Boolean
) : IBaseItem