package it.devddk.hackernewsclient.domain.repository

interface UpdateRepository {
    fun getLastItemAdded() : Result<ItemRepository>

}