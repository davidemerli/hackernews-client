package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.collection.ItemCollectionEntry
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime


data class Item(
    /**
     * The unique HackerNews ID of the Item
     */
    val id: Int,
    /**
     * The HackerNews type of the item (Comment, Story, Job)
     */
    val type: ItemType,
    /**
     * Is the item deleted by the user or by a mod
     */
    val deleted: Boolean = false,
    /**
     * The username of the user that submitted this item to HackerNEws
     */
    val by: String?,
    /**
     * When the user posted this Item to HackerNews
     */
    val time: LocalDateTime?,
    /**
     * When this item was downloaded from internet (last update)
     */
    val downloaded: LocalDateTime? = null,
    /**
     * dead
     */
    val dead: Boolean = false,
    /**
     * The [id] of the super-comment or story that contains this comment
     */
    val parent: ItemId? = null,
    /**
     * The [id] of the root story that contains this item. Not always available
     */
    val storyId: ItemId? = null,
    /**
     * The text of the Item. It is null for stories or jobs (that usually contain a link).
     * It could be null also for some removed/dead comments
     */
    val text: String? = null,
    /**
     * The list of all sub-comments of this item. Empty if none contained
     */
    val kids: List<ItemId> = emptyList(),
    /**
     * The title of this Item, usually never null for root items and null for comments
     * and pollopts
     */
    val title: String? = null,
    /**
     * Title of the story related to this comment, null if not a comment or not available
     */
    val storyTitle : String? = null,
    /**
     * Number of not cancelled and not dead descentants (comments)
     */
    val descendants: Int? = 0,
    /**
     * The list of all ids of all the choice of the poll
     */
    val parts: List<ItemId> = emptyList(),
    /**
     * The id of the poll item. Null if this item is not a pollopt
     */
    val poll: ItemId? = null,
    /**
     * The number of upvotes in case of comment/Story/job, the number of votes in case
     * of a poll
     */
    val score: Int? = null,
    /**
     * The url of the item
     */
    val url: String? = null,
    /**
     * The url of the preview of the item
     */
    val previewUrl: String? = null,
    /**
     * A map containing as keys the [UserDefinedItemCollection] to which this item is a part of and
     * as values an [ItemCollectionEntry] that contains more info o
     */
    val collections: Map<UserDefinedItemCollection, ItemCollectionEntry> = emptyMap(),
    /**
     * The saved html for the page
     */
    val htmlPage: String? = null,
)

val Map<UserDefinedItemCollection, ItemCollectionEntry>.favorite
    get() = contains(UserDefinedItemCollection.Favorites)

val Map<UserDefinedItemCollection, ItemCollectionEntry>.readLater
    get() = contains(UserDefinedItemCollection.ReadLater)