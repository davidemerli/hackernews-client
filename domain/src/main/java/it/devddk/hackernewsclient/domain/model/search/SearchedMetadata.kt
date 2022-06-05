package it.devddk.hackernewsclient.domain.model.search

import it.devddk.hackernewsclient.domain.model.utils.ItemId

/**
 * Additional data related to Algolia search results
 */
data class SearchResultMetaData(
    /**
     * The id of the current item
     */
    val id: ItemId,
    /**
     * The rank of the search result
     * 0 is the most relevant result
     * The higher the less result
     */
    val position: Int,
    /**
     * The id of the root story of this search result
     * If this result is a root element this field will be equal to [id]
     */
    val rootId: ItemId,
    /**
     * Contains information of what field to highlight
     */
    val highlightResult : HighlightData
)

/**
 * Contains all field that could be highlighted when there is a match with the search keywokrs
 */
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
    /**
     * The HTML text of a [Item] field that matches with the search query. The matching part
     * is wrapped in an HTML em tag.
     */
    val value: String?,
    /**
     * Specifies the type of the match
     * full, partial or none
     */
    val matchLevel: String,
    /**
     * List of all the words in the text that matched, fully or partially
     */
    val matchedWords: List<String>,
)
