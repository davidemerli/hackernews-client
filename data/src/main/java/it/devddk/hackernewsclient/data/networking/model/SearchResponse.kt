package it.devddk.hackernewsclient.data.networking.model

import com.google.gson.annotations.SerializedName
import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.data.networking.utils.toLocalDateTime
import it.devddk.hackernewsclient.data.networking.utils.toItemType
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.search.*
import timber.log.Timber
import java.time.LocalDateTime

data class SearchResponse(
    val hits: List<SearchResultHitResponse>,
    val page: Int,
    val nbHits: Int,
    val hitsPerPage: Int,
    val processingTimeMS: Int,
    val query: String,
    val params: String,
) : DomainMapper<SearchResultsSlice> {
    override fun mapToDomainModel(): SearchResultsSlice {
        val startPosition = page * hitsPerPage
        val results = hits.mapIndexed { relativePosition, hit ->
            val item = buildItemFromHit(hit)
            val metadata = SearchResultMetaData(
                id = hit.objectId.toInt(),
                position = startPosition + relativePosition,
                highlightResult = hit.highlightResult.mapToDomainModel()
            )
            SearchResult(item, metadata)
        }
        return SearchResultsSlice(page, startPosition, results)
    }

    private fun buildItemFromHit(hit : SearchResultHitResponse) : Item {
        val itemType = hit.type.toItemType() ?: ItemType.STORY
        Timber.d("Object id ${hit.objectId}")
        return Item(
            id = hit.objectId.toInt(),
            type = itemType,
            deleted = false,
            dead = false,
            downloaded = LocalDateTime.now(),
            by = hit.author,
            time = hit.created?.toLocalDateTime(),
            parent = hit.parentId,
            text = when(itemType) {
                ItemType.COMMENT -> hit.commentText
                else -> hit.storyText
            },
            // Kids not provided
            kids = emptyList(),
            title = hit.title,
            descendants = hit.numComments,
            // Parent poll not provided
            parts = emptyList(),
            poll = null,
            score = hit.points,
            url = hit.url
        )
    }
}

data class SearchResultHitResponse(
    val title: String,
    val url: String,
    val author: String?,
    val points: Int?,
    val type: String?,
    @SerializedName("story_text") val storyText: String?,
    @SerializedName("comment_text") val commentText: String?,
    @SerializedName("story_id") val storyId: Int?,
    @SerializedName("story_title") val storyTitle: String?,
    @SerializedName("story_url") val storyUrl: String?,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("created_at_i") val created: Long?,
    @SerializedName("relevancy_score") val relevancyScore: Int?,
    val tags: List<String>,
    @SerializedName("num_comments") val numComments: Int?,
    @SerializedName("objectID") val objectId: String,
    @SerializedName("_highlightResult") val highlightResult : HighlightDataResponse
)

data class HighlightDataResponse(
    val title: HighlightFieldResponse?,
    val url: HighlightFieldResponse?,
    val author: HighlightFieldResponse?,
    @SerializedName("comment_text") val commentText: HighlightFieldResponse?,
    @SerializedName("story_title") val storyTitle: HighlightFieldResponse?,
    @SerializedName("story_url") val storyUrl: HighlightFieldResponse?,
    @SerializedName("story_text") val storyText: HighlightFieldResponse?
) : DomainMapper<HighlightData> {
    override fun mapToDomainModel(): HighlightData {
        return HighlightData(
            title = title?.toHighlightFieldIfMatches(),
            url = url?.toHighlightFieldIfMatches(),
            author = author?.toHighlightFieldIfMatches(),
            commentText = commentText?.toHighlightFieldIfMatches(),
            storyTitle = storyTitle?.toHighlightFieldIfMatches(),
            storyUrl = storyUrl?.toHighlightFieldIfMatches(),
            storyText = storyText?.toHighlightFieldIfMatches()
        )
    }

}

data class HighlightFieldResponse(
    val value: String?,
    val matchLevel: String?,
    val matchedWords: List<String>?,
    val fullyHighlighted: Boolean?
) {
    fun toHighlightFieldIfMatches(): HighlightField? {
        if(matchLevel == null || matchLevel == "none") {
            return null
        }
        return HighlightField(
            value = value,
            // TODO Define proper sealed class for match level if needed
            matchLevel = matchLevel,
            matchedWords = matchedWords ?: emptyList()
        )
    }
}
