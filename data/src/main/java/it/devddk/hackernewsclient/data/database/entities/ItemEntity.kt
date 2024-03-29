package it.devddk.hackernewsclient.data.database.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.data.networking.utils.toItemType
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Keep
@Entity(tableName = "items", primaryKeys = ["id"])
data class ItemEntity(
    val id: Int,
    val deleted: Boolean,
    @ColumnInfo(name = "author") val by: String?,
    val storyId: Int?,
    val type: String,
    val time: LocalDateTime?,
    val saved: LocalDateTime?,
    val downloaded: LocalDateTime?,
    val dead: Boolean,
    val parent: Int?,
    val text: String?,
    val kids: List<Int>,
    val title: String?,
    val storyTitle: String?,
    val descendants: Int?,
    val parts: List<Int>,
    val poll: Int?,
    val score: Int?,
    val url: String?,
    val htmlPage: String?,
) : DomainMapper<Item> {

    override fun mapToDomainModel(): Item {
        return Item(
            id,
            type.toItemType() ?: ItemType.STORY,
            deleted,
            by,
            time,
            downloaded,
            dead,
            parent,
            storyId,
            text,
            kids,
            title,
            storyTitle,
            descendants,
            parts,
            poll,
            score,
            url,
            htmlPage = htmlPage
        )
    }

    override fun toString(): String {
        return "ItemEntity(\n" +
                "id=$id\n" +
                "deleted=$deleted\n," +
                "author=$by\n" +
                "storyId=$storyId\n" +
                "type=$type\n" +
                "time=$time\n" +
                "saved=$saved\n" +
                "downloaded=$downloaded\n" +
                "dead=$dead\n" +
                "parent=$parent\n" +
                "text=$text\n" +
                "kids=$kids\n" +
                "storyTitle=$storyTitle\n" +
                "descendants=$descendants\n" +
                "parts=$parts\n" +
                "poll=$poll\n" +
                "score=$score\n" +
                "url=$url\n" +
                "htmlPage=$htmlPage\n)"
    }
}


fun Item.toItemEntity(): ItemEntity {
    return ItemEntity(
        id = id,
        deleted = deleted,
        by = by,
        type = type.toString(),
        time = time,
        downloaded = downloaded,
        dead = dead,
        parent = parent,
        text = text,
        kids = kids,
        title = title,
        storyTitle = storyTitle,
        descendants = descendants,
        parts = parts,
        poll = poll,
        score = score,
        url = url,
        htmlPage = htmlPage,
        saved = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
        storyId = storyId
    )
}
