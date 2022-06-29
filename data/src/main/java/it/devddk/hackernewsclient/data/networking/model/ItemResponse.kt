package it.devddk.hackernewsclient.data.networking.model

import it.devddk.hackernewsclient.data.common.utils.ResponseConversionException
import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.data.networking.utils.convertTimestamp
import it.devddk.hackernewsclient.data.networking.utils.fixLinks
import it.devddk.hackernewsclient.data.networking.utils.toItemType
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import okio.ByteString.Companion.encode
import org.jsoup.Jsoup
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

fun getPreview(url: String?, itemId: ItemId): String? {
    url?.let {
        try {
            val response = Jsoup.connect(url)
                .ignoreContentType(true)
                .followRedirects(true)
                .execute()

            return response.parse()
                .select("meta[property=og:image]")
                .first()
                .attr("content")
        } catch (_: Exception) {}
    }

    return "https://hash-bg.davidemerli.com/$itemId"
}

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

    override fun mapToDomainModel(): Item {
        return Item(
            id ?: throw ResponseConversionException("id must specified in item response"),
            type?.toItemType()?: throw ResponseConversionException("type must specified in item response"),
            deleted,
            by,
            time?.convertTimestamp() ?: throw ResponseConversionException("time must specified in item response"),
            dead,
            parent,
            text?.fixLinks() ?: "",
            kids,
            title,
            descendants,
            parts,
            poll,
            score,
            url,
            getPreview(url, id)
        )
    }
}
