package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.items.BaseItem
import it.devddk.hackernewsclient.domain.model.items.MainItem

interface StoryRepository {

    enum class QueryType(maxNum : Int) {
        NEW_STORIES(500),
        TOP_STORIES(500),
        BEST_STORIES(500),
        ASK_STORIES(200),
        SHOW_STORIES(200),
        JOB_STORIES(200)
    }


    suspend fun getNewStories(query : QueryType): Result<List<MainItem>>
}