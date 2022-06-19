package it.devddk.hackernewsclient.domain.model.utils

import it.devddk.hackernewsclient.domain.model.items.Item

sealed class ItemCollection(val entryName : String)

sealed class HNItemCollection(entryName : String, val maxAmount : Int) : ItemCollection(entryName)

val ALL_QUERIES = listOf(TopStories, NewStories, JobStories, ShowStories, BestStories, AskStories)

object NewStories : HNItemCollection("New Stories", 500)
object TopStories : HNItemCollection("Top Stories", 500)
object BestStories : HNItemCollection("Best Stories", 500)
object AskStories : HNItemCollection("Ask HN", 200)
object ShowStories : HNItemCollection("Show HN",200)
object JobStories : HNItemCollection("HN Jobs", 200)

sealed class SavedItemCollection(entryName: String, val availableOffline : Boolean, val itemFilter : (Item) -> Boolean) : ItemCollection(entryName) {

    companion object {
        fun valueOf(name: String) : SavedItemCollection? {
            return when(name) {
                Favorite::class.simpleName -> Favorite
                ReadLater::class.simpleName -> ReadLater
                ApplyLater::class.simpleName -> ApplyLater
                else -> null
            }
        }
    }

    object Favorite : SavedItemCollection ("Favorites",true, { true })
    object ReadLater  : SavedItemCollection ("ReadLater", true, { true })
    object ApplyLater : SavedItemCollection ("ApplyLater", true, { true })


}
