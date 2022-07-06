package it.devddk.hackernewsclient.domain.interaction.collection

import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetCollectionsUseCase {
    suspend operator fun invoke(id: ItemId) : Result<Set<UserDefinedItemCollection>>
}

class GetCollectionsUseCaseImpl : GetCollectionsUseCase, KoinComponent {

    private val itemRepository: ItemRepository by inject()

    override suspend fun invoke(id: ItemId): Result<Set<UserDefinedItemCollection>> {
        return itemRepository.getCollectionsOfItem(id)
    }

}