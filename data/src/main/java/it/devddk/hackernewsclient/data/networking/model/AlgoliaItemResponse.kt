package it.devddk.hackernewsclient.data.networking.model

import com.google.gson.annotations.SerializedName
import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.data.networking.utils.toLocalDateTime
import it.devddk.hackernewsclient.data.networking.utils.toItemType
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemTree

data class AlgoliaItemResponse(
    @SerializedName("objectID") val id: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("created_at_i") val createdAtInstant: Long?,
    val type: String?,
    val author: String?,
    val title: String?,
    val url: String?,
    val text: String?,
    val points: Int?,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("story_id") val storyId: Int?,
    val children: List<AlgoliaItemResponse>,
    val options: List<AlgoliaItemResponse>
    ) : DomainMapper<ItemTree> {
    override fun mapToDomainModel(): ItemTree {
        val childrenTree = children.map {
            it.mapToDomainModel()
        }
        val optionsTree = options.map {
            it.mapToDomainModel()
        }
        val descendants = childrenTree.fold(0) {
            acc, subTree -> acc + subTree.comments.size + 1
        }
        return ItemTree(storyId ?: id.toInt(), mapToItem(descendants), childrenTree, optionsTree)
    }

    private fun mapToItem(descendants: Int) = Item(
        id = id.toInt(),
        type = type.toItemType()!!,
        deleted = false,
        by = author,
        time = createdAtInstant?.toLocalDateTime(),
        dead = false,
        text = text,
        kids = children.map { it.id.toInt() },
        title = title,
        // This should be calculated when expanding the tree
        descendants = descendants,
        parts = options.map { it.id.toInt() },
        poll = parentId,
        score = points,
        url = url,
        previewUrl = null
    )
}