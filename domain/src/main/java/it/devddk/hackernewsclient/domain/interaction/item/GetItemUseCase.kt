package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.items.Item


interface GetItemUseCase {


    suspend operator fun invoke(id: Int): Result<Item>
}