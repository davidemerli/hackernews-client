package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RefreshAllItemsUseCase {

    suspend operator fun invoke(): Unit
}

class RefreshAllItemsUseCaseImpl : RefreshAllItemsUseCase, KoinComponent {

    private val itemRepository : ItemRepository by inject()

    override suspend fun invoke() {
        itemRepository.invalidateCache()
    }

}