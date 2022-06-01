package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemTree
import it.devddk.hackernewsclient.domain.repository.CommentTreeRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetCommentTreeUseCase {
    suspend operator fun invoke(id: Int): Result<ItemTree>
}

class GetCommentTreeUseCaseImpl : GetCommentTreeUseCase, KoinComponent  {

    private val commentTreeRepository: CommentTreeRepository by inject()

    override suspend operator fun invoke(id: Int): Result<ItemTree> {
        return commentTreeRepository.getCommentTree(id)
    }
}