package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection


interface ItemRepository {

    sealed class ItemSources

    object Online : ItemSources()

    object Offline : ItemSources()

    object Cache : ItemSources()

    sealed class FetchMode(open val fetchOrder : List<ItemSources>) {

        fun onConnectivityAbsent() : FetchMode {
            return CustomFetch(fetchOrder.filter { it !is Online })
        }
    }

    object OfflineFirst : FetchMode(listOf(Cache, Offline, Online))

    object OnlineFirst : FetchMode(listOf(Cache, Online, Offline))

    object OnlyOnline : FetchMode(listOf(Cache, Online))

    object OnlyOffline : FetchMode(listOf(Cache, Offline))

    data class CustomFetch(override val fetchOrder: List<ItemSources>) : FetchMode(fetchOrder)

    /**
     * Gets an item by is [ItemId]
     */
    suspend fun getItemById(itemId: ItemId, fetchStrategy : FetchMode = OnlineFirst): Result<Item>

    suspend fun saveItem(item : Item)

    suspend fun addItemToCollection(id: ItemId, collection: UserDefinedItemCollection) : Result<Unit>

    suspend fun removeItemFromCollection(id: ItemId, collection: UserDefinedItemCollection) : Result<Unit>

    suspend fun getCollectionsOfItem(id: ItemId) : Result<Set<UserDefinedItemCollection>>

    suspend fun getAllItemsForCollection(collection: UserDefinedItemCollection) : Result<List<ItemId>>

    suspend fun invalidateCache() {
        // Do nothing by default, if cache available in implementation, handle invalidate
    }
}