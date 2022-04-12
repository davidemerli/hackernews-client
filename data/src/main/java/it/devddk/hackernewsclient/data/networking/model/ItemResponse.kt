package it.devddk.hackernewsclient.data.networking.model

import it.devddk.hackernewsclient.data.common.utils.ResponseConversionException
import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.domain.model.items.*
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import java.lang.NullPointerException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.HashMap

data class ItemResponse(
    val id: Int,
    val type: String,
    val deleted: Boolean = false,
    val by: String?,
    val time: Long,
    val dead: Boolean = false,
    val parent: Int?,
    val text: String?,
    val kids: List<Int> = emptyList(),
    val title: String?,
    val descendants: Int?,
    val parts: List<Int> = emptyList(),
    val poll: Int?,
    val score: Int?,
    val url: String?,
) : DomainMapper<Item> {
    override fun mapToDomainModel(): Item {
        return Item(
            id,
            itemResponse(type),
            deleted,
            by?.let { Expandable.compressed(it) },
            convertTimestamp(time),
            dead,
            parent?.let { Expandable.compressed(it) },
            text,
            kids.associateWith { null },
            title,
            descendants,
            parts.associateWith { null },
            poll?.let { Expandable.compressed(it) },
            score,
            url
        )
    }

    private fun itemResponse(typeStr : String) : ItemType {
        return when(typeStr) {
            "story" -> ItemType.STORY
            "poll"  -> ItemType.POLL
            "pollopt" -> ItemType.POLL_OPT
            "job" -> ItemType.JOB
            "comment" -> ItemType.COMMENT
            else -> throw ResponseConversionException("Invalid type")
        }
    }

    private fun convertTimestamp(timestamp: Long): LocalDateTime =
        Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).toLocalDateTime()
}
