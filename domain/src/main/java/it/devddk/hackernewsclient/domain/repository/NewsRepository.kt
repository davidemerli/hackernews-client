package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.NewsInfo

interface NewsRepository {
    suspend fun getNewsById(newsId: Int): Result<NewsInfo>
}
