package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

data class StoryItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: User,
    override val time: LocalDateTime,
    override val dead: Boolean = false,
    override val kids: List<BaseItem>? = null,
    val descendants : Int?,
    val score : Int?,
    val title : String?,
    val url : String?
) : BaseItem {
    override val type : ItemType = ItemType.STORY
}