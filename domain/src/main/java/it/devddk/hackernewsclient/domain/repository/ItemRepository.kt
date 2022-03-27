package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.NewsInfo
import it.devddk.hackernewsclient.domain.model.items.BaseItem

interface ItemRepository {
    suspend fun getItemById(itemId: Int): Result<BaseItem>
}