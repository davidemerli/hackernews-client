package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.utils.SavedItemCollection
import java.time.LocalDateTime


data class Item(
    val id: Int,
    val type: ItemType,
    val deleted: Boolean = false,
    val by: String?,
    val time: LocalDateTime?,
    val dead: Boolean = false,
    val parent: ItemId? = null,
    val text: String? = null,
    val kids: List<ItemId> = emptyList(),
    val title: String? = null,
    val descendants: Int? = 0,
    val parts: List<ItemId> = emptyList(),
    val poll: ItemId? = null,
    val score: Int? = null,
    val url: String? = null,
    val previewUrl: String? = null,
    val collections : Set<SavedItemCollection> = emptySet(),
    val htmlPage: String? = null
)