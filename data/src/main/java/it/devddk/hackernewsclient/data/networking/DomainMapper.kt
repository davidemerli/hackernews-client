package it.devddk.hackernewsclient.data.networking


interface DomainMapper<T : Any> {
    fun mapToDomainModel(): T
}