package hackernewsclient


import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.domain.model.collection.ItemCollectionEntry
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection.Companion.ALL_USER_QUERIES
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

fun generateRandomItem(
    id: Int = Random.nextInt(1000000),
    type: ItemType = ItemType.values().random(),
    deleted: Boolean = Random.nextBoolean(),
    by: String? = "user${Random.nextInt(1000000)}",
    time: LocalDateTime? = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    downloaded: LocalDateTime? = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    dead: Boolean = Random.nextBoolean(),
    parent: ItemId? = Random.nextInt(1000000),
    storyId: ItemId? = Random.nextInt(1000000),
    text: String? = "text${Random.nextInt(1000000)}",
    kids: List<ItemId> = emptyList(),
    title: String? = "title${Random.nextInt(1000000)}",
    storyTitle: String? = null,
    descendants: Int? = 0,
    parts: List<ItemId> = emptyList(),
    poll: ItemId? = Random.nextInt(1000000),
    score: Int? = Random.nextInt(1000000),
    url: String? = "site${Random.nextInt(1000000)}.com",
    previewUrl: String? = "preview${Random.nextInt(1000000)}.com",
    collections: Map<UserDefinedItemCollection, ItemCollectionEntry> =
        ALL_USER_QUERIES.filter { Random.nextBoolean() }.associateWith { key ->
            ItemCollectionEntry(
                key,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
        },
    htmlPage: String? = null,
) = Item(
    id,
    type,
    deleted,
    by,
    time,
    downloaded,
    dead,
    parent,
    storyId,
    text,
    kids,
    title,
    storyTitle,
    descendants,
    parts,
    poll,
    score,
    url,
    previewUrl,
    collections,
    htmlPage
)

fun generateRandomItemCollectionEntity(
    id : Int = Random.nextInt(1000000),
    collection : UserDefinedItemCollection = ALL_USER_QUERIES.random(),
    timeAdded : LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
) = ItemCollectionEntity(
    id, collection, timeAdded
)