package it.devddk.hackernewsclient.data.networking.model

import it.devddk.hackernewsclient.data.common.utils.ResponseConversionException
import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import okio.ByteString.Companion.encode
import org.jsoup.Jsoup
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

fun getPreview(siteUrl: String?): String? {
    try {
        if (siteUrl != null) {
            val response =
                Jsoup.connect(siteUrl).ignoreContentType(true).followRedirects(true).execute()

            return response.parse()
                .select("meta[property=og:image]")
                .first()
                .attr("content")
        }
    } catch (e: Exception) {
    }

    return if (siteUrl != null) {
        val urlEncoded = siteUrl.encode().base64()

        "https://identicon-api.herokuapp.com/$urlEncoded/128?format=png"
    } else {
        "https://identicon-api.herokuapp.com/${Random.nextLong()}/128?format=png"
    }
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
            itemResponse(type
                ?: throw ResponseConversionException("type must specified in item response")),
            deleted,
            by,
            convertTimestamp(time
                ?: throw ResponseConversionException("time must specified in item response")),
            dead,
            parent,
            text?.let { fixLinks(text) } ?: "",
            kids,
            title,
            descendants,
            parts,
            poll,
            score,
            url,
            getPreview(url)
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

    private fun fixLinks(text: String): String {
        /*
         get all links and replace text with link, since longer links on HN arrive with ellipses
         in the text part of the <a> tag. The html parser does not make clickable links, so we need
         to do it manually, but the text presents the link with ellipses, so we need to restore them
         to full length.
         */

        val doc = Jsoup.parse(text)

        doc.select("a").forEach {
            val href = it.attr("href")
            it.text(href)
        }

        return doc.html()

    }

    private fun convertTimestamp(timestamp: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC)
}
