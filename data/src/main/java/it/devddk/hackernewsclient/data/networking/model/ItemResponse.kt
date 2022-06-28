package it.devddk.hackernewsclient.data.networking.model

import it.devddk.hackernewsclient.data.common.utils.ResponseConversionException
import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.data.networking.utils.toLocalDateTime
import it.devddk.hackernewsclient.data.networking.utils.fixLinks
import it.devddk.hackernewsclient.data.networking.utils.toItemType
import it.devddk.hackernewsclient.domain.model.items.Item
import okio.ByteString.Companion.encode
import org.jsoup.Jsoup
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
            type?.toItemType()?: throw ResponseConversionException("type must specified in item response"),
            deleted,
            by,
            time?.toLocalDateTime() ?: throw ResponseConversionException("time must specified in item response"),
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
            getPreview(url)
        )

    }


}
