package it.devddk.hackernewsclient.domain.interaction.news

import it.devddk.hackernewsclient.domain.repository.NewsRepository

class GetNewsUseCaseImpl(private val newsRepository: NewsRepository) : GetNewsUseCase {
    override suspend operator fun invoke(param: Int) = newsRepository.getNewsById(param)
}
