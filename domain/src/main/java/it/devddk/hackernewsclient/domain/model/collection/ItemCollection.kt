package it.devddk.hackernewsclient.domain.model.collection

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType

sealed class ItemCollection(val entryName: String)

sealed class HNItemCollection(entryName: String, val maxAmount: Int) : ItemCollection(entryName)

val ALL_QUERIES = listOf(TopStories, NewStories, JobStories, ShowStories, BestStories, AskStories,
    UserDefinedItemCollection.Favorites, UserDefinedItemCollection.ReadLater)

object NewStories : HNItemCollection("New Stories", 500)
object TopStories : HNItemCollection("Top Stories", 500)
object BestStories : HNItemCollection("Best Stories", 500)
object AskStories : HNItemCollection("Ask HN", 200)
object ShowStories : HNItemCollection("Show HN", 200)
object JobStories : HNItemCollection("HN Jobs", 200)

sealed class UserDefinedItemCollection(
    entryName: String,
    val saveWholeItem: Boolean,
    val allowReinsertion: Boolean,
    val itemFilter: (Item) -> Boolean,
) : ItemCollection(entryName) {

    companion object {
        fun valueOf(name: String): UserDefinedItemCollection? {
            return when (name) {
                Favorites::class.simpleName -> Favorites
                ReadLater::class.simpleName -> ReadLater
                VisitedItem::class.simpleName -> VisitedItem
                else -> null
            }
        }

        val ALL_USER_QUERIES = listOf(Favorites, ReadLater, VisitedItem)
    }

    object Favorites : UserDefinedItemCollection("Favorites",true, false, { true })
    object ReadLater : UserDefinedItemCollection("ReadLater", true, false, { true })
    object VisitedItem : UserDefinedItemCollection("ReadItem", false, true, { it.type != ItemType.COMMENT })
}
