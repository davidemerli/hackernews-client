package it.devddk.hackernewsclient.domain.model.collection

import androidx.annotation.Keep
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType

@Keep
sealed class ItemCollection(val entryName: String)

@Keep
sealed class HNItemCollection(entryName: String, val maxAmount: Int) : ItemCollection(entryName)

val ALL_QUERIES = listOf(
    TopStories,
    NewStories,
    JobStories,
    ShowStories,
    BestStories,
    AskStories,
    UserDefinedItemCollection.Favorites,
    UserDefinedItemCollection.ReadLater
)

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
data class UserStories(val name: String) : ItemCollection("User $name")

@Keep
sealed class UserDefinedItemCollection(
    entryName: String,
    val saveWholeItem: Boolean,
    val allowReinsertion: Boolean,
    val itemFilter: (Item) -> Boolean,
) : ItemCollection(entryName) {

    companion object {
        fun valueOf(name: String): UserDefinedItemCollection? {
            return when (name) {
                Favorites.entryName -> Favorites
                ReadLater.entryName -> ReadLater
                VisitedItem.entryName -> VisitedItem
                else -> null
            }
        }

        val ALL_USER_QUERIES = listOf(Favorites, ReadLater, VisitedItem)
    }


    //TODO: avoid using classNames
    @Keep
    object Favorites : UserDefinedItemCollection("Favorites", true, false,{ true })
    @Keep
    object ReadLater : UserDefinedItemCollection("ReadLater", true, false,{ true })
    @Keep
    object VisitedItem : UserDefinedItemCollection("VisitedItem", false, true, { it.type != ItemType.COMMENT })

    override fun toString(): String {
        return entryName
    }

}
