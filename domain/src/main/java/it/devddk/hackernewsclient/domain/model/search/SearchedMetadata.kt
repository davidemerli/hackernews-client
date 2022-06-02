package it.devddk.hackernewsclient.domain.model.search

import it.devddk.hackernewsclient.domain.model.utils.ItemId


data class SearchResultMetaData(
    val id: ItemId,
    val position: Int,
    val highlightResult : HighlightData
)

data class HighlightData(
    val title: HighlightField?,
    val url: HighlightField?,
    val author: HighlightField?,
    val commentText: HighlightField?,
    val storyTitle: HighlightField?,
    val storyUrl: HighlightField?,
    val storyText: HighlightField?
)

data class HighlightField(
    val value: String?,
    val matchLevel: String,
    val matchedWords: List<String>,
)
