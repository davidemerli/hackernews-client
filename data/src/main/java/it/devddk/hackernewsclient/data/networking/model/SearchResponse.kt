package it.devddk.hackernewsclient.data.networking.model

import com.google.gson.annotations.SerializedName


data class SearchResponse(
    val hits: List<SearchResultHitResponse>,
    val page: Int,
    val nbHits: Int,
    val hitsPerPage: Int,
    val processingTimeMS: Int,
    val query: String,
    val params: String,
)

data class SearchResultHitResponse(
    val title: String,
    val url: String,
    val author: String?,
    val points: Int?,
    @SerializedName("story_text") val storyText: String?,
    @SerializedName("comment_text") val commentText: String?,
    @SerializedName("comment_text") val tags: List<String>?,
    @SerializedName("num_comments") val numComments: Int?,
    val objectId: String,
    @SerializedName("_highlightResult") val highlightResult : HighlightResultResponse
)

data class HighlightResultResponse(
    val title: HighlightResultItemResponse,
    val url: HighlightResultItemResponse,
    val author: HighlightResultItemResponse,
)

data class HighlightResultItemResponse(
    val value: String,
    val matchLevel: String,
    val matchedWords: List<String>,
)