package it.devddk.hackernewsclient.domain.model.collection

import it.devddk.hackernewsclient.domain.model.items.Item

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
    val availableOffline: Boolean,
    val itemFilter: (Item) -> Boolean,
) : ItemCollection(entryName) {

    companion object {
        fun valueOf(name: String): UserDefinedItemCollection? {
            return when (name) {
                Favorites::class.simpleName -> Favorites
                ReadLater::class.simpleName -> ReadLater
                ApplyLater::class.simpleName -> ApplyLater
                else -> null
            }
        }
    }

    object Favorites : UserDefinedItemCollection("Favorites", true, { true })
    object ReadLater : UserDefinedItemCollection("ReadLater", true, { true })
    object ApplyLater : UserDefinedItemCollection("ApplyLater", true, { true })


}
