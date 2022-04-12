package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.items.ICommentableItem

interface GetItemDescendantsUseCase {

    companion object {
        const val DEPTH_INFINITE = 0
    }


    suspend operator fun invoke(item : ICommentableItem, depth : Int = DEPTH_INFINITE) : ICommentableItem {
        return invoke(item.id, depth)
    }

    suspend operator fun invoke(id : Int, depth: Int = DEPTH_INFINITE) : ICommentableItem
}