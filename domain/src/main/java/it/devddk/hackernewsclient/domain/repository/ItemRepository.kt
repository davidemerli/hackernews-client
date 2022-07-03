package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.ItemTree


interface ItemRepository {

    companion object {

        val OfflineFirst = FetchMode(Cache, Offline, Online)

        val OnlineFirst = FetchMode(Cache, Online, Offline)

        val OnlyOnline = FetchMode(Cache, Online)

        val OnlyOffline = FetchMode(Cache, Offline)

        const val MIN_TIME_FOR_REFRESH_SECS = 900

    }

    data class FetchMode(val fetchOrder : List<ItemSources>) {

        constructor(vararg items : ItemSources) : this(listOf(*items))

        fun onConnectivityAbsent() : FetchMode {
            return FetchMode(fetchOrder.filter { it !is Online })
        }
    }

    sealed class ItemSources

    object Online : ItemSources()

    object Offline : ItemSources()

    object Cache : ItemSources()

    /**
     * Gets an item by is [ItemId]
     */
    suspend fun getItemById(itemId: ItemId, fetchStrategy : FetchMode = OnlineFirst): Result<Item>

    suspend fun saveItem(item : ItemId) : Result<Unit>

    suspend fun addItemToCollection(id: ItemId, collection: UserDefinedItemCollection) : Result<Unit>

    suspend fun removeItemFromCollection(id: ItemId, collection: UserDefinedItemCollection) : Result<Unit>

    suspend fun getCollectionsOfItem(id: ItemId) : Result<Set<UserDefinedItemCollection>>

    suspend fun getAllItemsForCollection(collection: UserDefinedItemCollection) : Result<List<ItemId>>

    suspend fun getCommentTree(id : ItemId) : Result<ItemTree>

    suspend fun invalidateCache() {
        // Do nothing by default, if cache available in implementation, handle invalidate
    }
}