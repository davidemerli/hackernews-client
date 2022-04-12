package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.utils.ItemId

interface UpdateRepository {
    fun getLastItemAdded() : Result<ItemId>

}