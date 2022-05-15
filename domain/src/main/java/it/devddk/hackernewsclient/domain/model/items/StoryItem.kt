package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime

interface IStoryItem : IBaseItem, ICommentableItem {
    val title: String?
    val score: Int?
    val descendants: Int?
    val url: String?
    val previewUrl: String?
    val text: String?
}

data class StoryItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: Expandable<String, User>?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val kids: Map<ItemId, ICommentItem?>,
    override val title: String?,
    override val score: Int?,
    override val descendants: Int?,
    override val url : String?,
    override val previewUrl : String?,
    override val text : String?
) : IStoryItem {
    override val type: ItemType = ItemType.STORY

}