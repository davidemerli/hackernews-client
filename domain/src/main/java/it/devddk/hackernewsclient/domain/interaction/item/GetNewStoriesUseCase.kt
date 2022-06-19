package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.utils.HNItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.repository.StoryRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetNewStoriesUseCase {
    suspend operator fun invoke(
        query: HNItemCollection,
    ): Result<List<ItemId>>
}

class GetNewStoriesUseCaseImpl : GetNewStoriesUseCase, KoinComponent {

    private val storyRepository: StoryRepository by inject()

    override suspend fun invoke(
        query: HNItemCollection,
    ): Result<List<ItemId>> {
        return storyRepository.getStories(query)
    }
}