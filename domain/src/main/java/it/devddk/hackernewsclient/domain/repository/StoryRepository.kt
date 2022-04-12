package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.domain.model.utils.ItemId

interface StoryRepository {


    suspend fun getStories(query : CollectionQueryType): Result<List<ItemId>>
}