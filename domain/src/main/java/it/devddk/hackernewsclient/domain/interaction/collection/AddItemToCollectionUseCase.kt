package it.devddk.hackernewsclient.domain.interaction.collection

import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AddItemToCollectionUseCase {

    companion object {
        val PERSISTENCE_REFRESH_TIME_MILLIS = 5 * 60_000
    }

    suspend operator fun invoke(id: Int, collection : UserDefinedItemCollection) : Result<Unit>
}


class AddItemToCollectionUseCaseImpl : AddItemToCollectionUseCase, KoinComponent {

    private val itemRepository: ItemRepository by inject()

    override suspend fun invoke(id: Int, collection: UserDefinedItemCollection): Result<Unit> {
        return itemRepository.addItemToCollection(id, collection)
    }

}
