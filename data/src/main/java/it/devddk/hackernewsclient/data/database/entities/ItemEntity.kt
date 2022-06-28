package it.devddk.hackernewsclient.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.data.networking.base.RoomMapper
import it.devddk.hackernewsclient.data.networking.utils.toItemType
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import java.time.LocalDateTime

@Entity(tableName = "items", primaryKeys = ["id"])
data class ItemEntity(
    val id: Int,
    val deleted: Boolean,
    @ColumnInfo(name= "author") val by: String?,
    val storyId: Int?,
    val type: String,
    val time: LocalDateTime?,
    val saved: LocalDateTime?,
    val dead: Boolean,
    val parent: Int?,
    val text: String?,
    val kids: List<Int>,
    val title: String?,
    val descendants: Int?,
    val parts: List<Int>,
    val poll: Int?,
    val score: Int?,
    val url: String?,
    val htmlPage: String?
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
            url,
            htmlPage = htmlPage
        )
    }
}


fun Item.toItemEntity(storyId : Int? = null) : ItemEntity {
    return ItemEntity(
        id = id,
        deleted = deleted,
        by = by,
        type = type.toString(),
        time = time,
        dead = dead,
        parent = parent,
        text = text,
        kids = kids,
        title = title,
        descendants = descendants,
        parts = parts,
        poll = poll,
        score = score,
        url = url,
        htmlPage = htmlPage,
        saved = LocalDateTime.now(),
        storyId = null)
}




data class ItemEntityWithoutHtmlPage(
    val id: Int,
    val deleted: Boolean,
    @ColumnInfo(name= "author") val by: String?,
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
            url,
            htmlPage = null
        )
    }
}
