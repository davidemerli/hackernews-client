package it.devddk.hackernewsclient.domain.interaction.collection

import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RemoveItemFromCollectionUseCase {
    suspend operator fun invoke(item: Int, collection : UserDefinedItemCollection) : Result<Unit>
}

class RemoveItemFromCollectionUseCaseImpl : RemoveItemFromCollectionUseCase, KoinComponent {

    private val itemRepository: ItemRepository by inject()

    override suspend fun invoke(item: Int, collection : UserDefinedItemCollection): Result<Unit> {
        return itemRepository.removeItemFromCollection(item, collection)
    }

}