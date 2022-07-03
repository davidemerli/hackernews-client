package it.devddk.hackernewsclient.domain.model.collection

import androidx.annotation.Keep
import it.devddk.hackernewsclient.domain.model.items.Item

@Keep
sealed class ItemCollection(val entryName: String)

@Keep
sealed class HNItemCollection(entryName: String, val maxAmount: Int) : ItemCollection(entryName)

val ALL_QUERIES = listOf(TopStories, NewStories, JobStories, ShowStories, BestStories, AskStories,
    UserDefinedItemCollection.Favorites, UserDefinedItemCollection.ReadLater)

@Keep
object NewStories : HNItemCollection("New Stories", 500)
@Keep
object TopStories : HNItemCollection("Top Stories", 500)
@Keep
object BestStories : HNItemCollection("Best Stories", 500)
@Keep
object AskStories : HNItemCollection("Ask HN", 200)
@Keep
object ShowStories : HNItemCollection("Show HN", 200)
@Keep
object JobStories : HNItemCollection("HN Jobs", 200)

@Keep
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

    //TODO: avoid using classNames
    @Keep
    object Favorites : UserDefinedItemCollection("Favorites", true, { true })
    @Keep
    object ReadLater : UserDefinedItemCollection("ReadLater", true, { true })
    @Keep
    object ApplyLater : UserDefinedItemCollection("ApplyLater", true, { true })
}
