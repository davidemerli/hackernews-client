package it.devddk.hackernewsclient.domain.interaction.news

import it.devddk.hackernewsclient.domain.base.BaseUseCase
import it.devddk.hackernewsclient.domain.model.NewsInfo

interface GetNewsUseCase : BaseUseCase<Int, NewsInfo>{
    override suspend operator fun invoke(param: Int): Result<NewsInfo>
}
