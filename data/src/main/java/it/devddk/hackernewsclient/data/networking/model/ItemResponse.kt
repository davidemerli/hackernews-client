package it.devddk.hackernewsclient.data.networking.model

import com.kedia.ogparser.OpenGraphCallback
import com.kedia.ogparser.OpenGraphParser
import com.kedia.ogparser.OpenGraphResult
import it.devddk.hackernewsclient.data.common.utils.ResponseConversionException
import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class ItemResponse(
    val id: Int? = null,
    val type: String? = null,
    val deleted: Boolean = false,
    val by: String? = null,
    val time: Long? = null,
    val dead: Boolean = false,
    val parent: Int? = null,
    val text: String? = null,
    val kids: List<Int> = emptyList(),
    val title: String? = null,
    val descendants: Int? = null,
    val parts: List<Int> = emptyList(),
    val poll: Int? = null,
    val score: Int? = null,
    val url: String? = null,
) : DomainMapper<Item> {

    private var previewUrl: String? = null

    private val ogp = OpenGraphParser(object : OpenGraphCallback {
        override fun onPostResponse(openGraphResult: OpenGraphResult) {
            Timber.d(openGraphResult.image.toString())

            previewUrl = openGraphResult.image
        }

        override fun onError(error: String) {
            Timber.d(error)
        }
    })

    override fun mapToDomainModel(): Item {
        ogp.parse(url ?: "")

        return Item(
            id ?: throw ResponseConversionException("id must specified in item response"),
            itemResponse(type
                ?: throw ResponseConversionException("type must specified in item response")),
            deleted,
            by?.let { Expandable.compressed(it) },
            convertTimestamp(time
                ?: throw ResponseConversionException("time must specified in item response")),
            dead,
            parent?.let { Expandable.compressed(it) },
            text,
            kids.associateWith { null },
            title,
            descendants,
            parts.associateWith { null },
            poll?.let { Expandable.compressed(it) },
            score,
            url,
            previewUrl
        )
    }

    private fun itemResponse(typeStr: String): ItemType {
        return when (typeStr) {
            "story" -> ItemType.STORY
            "poll" -> ItemType.POLL
            "pollopt" -> ItemType.POLL_OPT
            "job" -> ItemType.JOB
            "comment" -> ItemType.COMMENT
            else -> throw ResponseConversionException("Invalid type")
        }
    }

    private fun convertTimestamp(timestamp: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC)
}
