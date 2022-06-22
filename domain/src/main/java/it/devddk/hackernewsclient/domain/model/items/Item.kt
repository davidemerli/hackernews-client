package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.collection.ItemCollectionEntry
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
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
    val collections: Map<UserDefinedItemCollection, ItemCollectionEntry> = emptyMap(),
    val htmlPage: String? = null,
)

val Map<UserDefinedItemCollection, ItemCollectionEntry>.favorite
    get() = contains(UserDefinedItemCollection.Favorites)

val Map<UserDefinedItemCollection, ItemCollectionEntry>.readLater
    get() = contains(UserDefinedItemCollection.ReadLater)