package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import it.devddk.hackernewsclient.domain.repository.StoryRepository
import it.devddk.hackernewsclient.domain.utils.skipped
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetNewStoriesUseCase {
    suspend operator fun invoke(
        query: CollectionQueryType,
        limit: Int = 0,
    ): Result<Map<ItemId, Result<Item>>>
}

class GetNewStoriesUseCaseImpl : GetNewStoriesUseCase, KoinComponent {

    val itemRepository: ItemRepository by inject()
    val storyRepository: StoryRepository by inject()

    override suspend fun invoke(
        query: CollectionQueryType,
        limit: Int,
    ): Result<Map<ItemId, Result<Item>>> {
        return storyRepository.getStories(query).mapCatching { stories ->
            return@mapCatching stories.mapIndexed { index, id ->
                if (index > limit) id to Result.skipped()
                else id to itemRepository.getItemById(id)
            }.toMap()
        }
    }
}