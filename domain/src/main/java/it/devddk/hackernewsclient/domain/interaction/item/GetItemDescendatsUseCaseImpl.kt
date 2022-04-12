package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.Failure
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import org.koin.java.KoinJavaComponent.inject
import java.lang.Exception
import java.util.*
/*
class GetItemDescendantsUseCaseImpl : GetItemDescendantsUseCase {

    private val itemRepository: ItemRepository by inject(ItemRepository::class.java)

    override suspend fun invoke(id: ItemId, depth: Int): Result<Item> {
        val x = itemRepository.getItemById(id)
        return Result.failure(Exception("aaa"))
    }

    private suspend fun bfs(initial: Item, maxDepth: Int) : Result<Item> {
        val bfsQueue: Queue<Pair<Int, Int>> = LinkedList()
        bfsQueue.offer(Pair(initial.id, 0))
        while (true) {
            val (id, depth) = bfsQueue.peek() ?: break
            val item = runCatching { itemRepository.getItemById(id) }
        }

    }

}
*/
