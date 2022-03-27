package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class StoryItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: User?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val kids: List<CommentItem>,
    override val title: String,
    override val score: Int,
    override val descendants: Int,
    val url : String = "",
    val text : String = ""
) : BaseItem, MainItem, CommentableItem {
    override val type: ItemType = ItemType.STORY

    val isAskHN : Boolean
        get() = text.isNotEmpty()
}