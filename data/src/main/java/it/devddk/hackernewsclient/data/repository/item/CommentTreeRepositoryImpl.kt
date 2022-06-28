package it.devddk.hackernewsclient.data.repository.item

import it.devddk.hackernewsclient.data.api.AlgoliaSearchApi
import it.devddk.hackernewsclient.data.networking.base.asResult
import it.devddk.hackernewsclient.domain.model.items.ItemTree
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.repository.CommentTreeRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CommentTreeRepositoryImpl : CommentTreeRepository, KoinComponent {

    private val algoliaApi: AlgoliaSearchApi by inject()

    override suspend fun getCommentTree(id: ItemId): Result<ItemTree> {
        return algoliaApi.getItemById(id).asResult().mapCatching {
            it.mapToDomainModel()
        }
    }
}