package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject

class GetItemUseCaseImpl : GetItemUseCase, KoinComponent {

    private val itemRepository : ItemRepository by inject(ItemRepository::class.java)

    override suspend operator fun invoke(id: Int): Result<Item> {
        return itemRepository.getItemById(id)
    }
}