package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.utils.Identifiable
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime

interface IBaseItem : Identifiable<ItemId> {

    companion object {
        inline fun <reified T : IBaseItem> itemCast(item : IBaseItem) : T? {
            return item as? T
        }
    }

    override val id: ItemId
    val type: ItemType
    val deleted: Boolean
    val by: String?
    val time: LocalDateTime?
    val dead: Boolean
}

data class BaseItem(
    override val id: ItemId,
    override val type: ItemType,
    override val deleted: Boolean,
    override val by: String?,
    override val time: LocalDateTime?,
    override val dead: Boolean
) : IBaseItem