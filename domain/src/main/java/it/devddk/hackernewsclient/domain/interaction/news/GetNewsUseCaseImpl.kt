package it.devddk.hackernewsclient.domain.interaction.news

class GetNewsUseCaseImpl(private val newsRepository: NewsRepository) : GetNewsUseCase {
    override suspend operator fun invoke(param: Int) = newsRepository.getNewsById(param)
}
