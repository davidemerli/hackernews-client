package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.collection.HNItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId

interface HNCollectionRepository {


    suspend fun getStories(query : HNItemCollection): Result<List<ItemId>>
}