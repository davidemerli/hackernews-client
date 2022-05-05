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
    ): Result<List<ItemId>>
}

class GetNewStoriesUseCaseImpl : GetNewStoriesUseCase, KoinComponent {

    private val storyRepository: StoryRepository by inject()

    override suspend fun invoke(
        query: CollectionQueryType,
    ): Result<List<ItemId>> {
        return storyRepository.getStories(query)
    }
}