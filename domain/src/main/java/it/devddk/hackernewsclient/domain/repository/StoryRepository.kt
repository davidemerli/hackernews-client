package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.utils.HNItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId

interface StoryRepository {


    suspend fun getStories(query : HNItemCollection): Result<List<ItemId>>
}