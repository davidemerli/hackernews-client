package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.collection.HNItemCollection
import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.collection.UserStories
import it.devddk.hackernewsclient.domain.repository.HNCollectionRepository
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import it.devddk.hackernewsclient.domain.repository.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetNewStoriesUseCase {
    suspend operator fun invoke(
        query: ItemCollection,
    ): Result<List<ItemId>>
}

class GetNewStoriesUseCaseImpl : GetNewStoriesUseCase, KoinComponent {

    private val hnCollectionRepository: HNCollectionRepository by inject()
    private val itemRepository: ItemRepository by inject()
    private val userRepository: UserRepository by inject()

    override suspend fun invoke(
        query: ItemCollection,
    ): Result<List<ItemId>> {
        return when(query) {
            is HNItemCollection -> hnCollectionRepository.getStories(query)
            is UserDefinedItemCollection -> itemRepository.getAllItemsForCollection(query)
            is UserStories -> userRepository.getUserByUsername(query.name).map { it.submitted }
        }
    }
}