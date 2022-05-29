package it.devddk.hackernewsclient.domain.repository

interface SearchRepository {
    suspend fun searchByRelevance(query : String)
}