package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.items.Item


interface ItemRepository {
    suspend fun getItemById(itemId: Int): Result<Item>

    suspend fun saveItem(item : Item)
}