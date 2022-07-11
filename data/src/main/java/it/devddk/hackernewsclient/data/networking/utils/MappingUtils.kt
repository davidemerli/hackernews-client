package it.devddk.hackernewsclient.data.networking.utils

import it.devddk.hackernewsclient.domain.model.items.ItemType
import org.jsoup.Jsoup
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun String?.toItemType(): ItemType? {
    return when (this) {
        "story","STORY" -> ItemType.STORY
        "poll","POLL" -> ItemType.POLL
        "pollopt","POLL_OPT" -> ItemType.POLL_OPT
        "job","JOB" -> ItemType.JOB
        "comment","COMMENT" -> ItemType.COMMENT
        else -> null
    }
}

fun String.fixLinks(): String {
    /*
     get all links and replace text with link, since longer links on HN arrive with ellipses
     in the text part of the <a> tag. The html parser does not make clickable links, so we need
     to do it manually, but the text presents the link with ellipses, so we need to restore them
     to full length.
     */

    val doc = Jsoup.parse(this)

    doc.select("a").forEach {
        val href = it.attr("href")
        it.text(href)
    }

    return doc.html()

}

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochSecond(this), ZoneId.systemDefault())

fun LocalDateTime.toLongTimestamp(): Long =
    this.atZone(ZoneId.systemDefault()).toEpochSecond()