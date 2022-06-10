package it.devddk.hackernewsclient.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.data.networking.base.RoomMapper
import it.devddk.hackernewsclient.data.networking.utils.toItemType
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import java.time.LocalDateTime

@Entity(tableName = "items", primaryKeys = ["source", "id"])
data class ItemEntity(
    val id: Int,
    val deleted: Boolean,
    val by: String?,
    val type: String,
    val time: LocalDateTime?,
    val dead: Boolean,
    val parent: Int?,
    val text: String?,
    val kids: List<Int>,
    val title: String?,
    val descendants: Int?,
    val parts: List<Int>,
    val poll: Int?,
    val score: Int?,
    val url: String?
) : DomainMapper<Item> {
    override fun mapToDomainModel(): Item {
        return Item(
            id,
            type.toItemType() ?: ItemType.STORY,
            deleted,
            by,
            time,
            dead,
            parent,
            text,
            kids,
            title,
            descendants,
            parts,
            poll,
            score,
            url
        )
    }

}
