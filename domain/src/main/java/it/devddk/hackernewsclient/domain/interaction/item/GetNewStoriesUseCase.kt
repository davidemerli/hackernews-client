package it.devddk.hackernewsclient.domain.interaction.item

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import it.devddk.hackernewsclient.domain.repository.StoryRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

interface GetNewStoriesUseCase {
    suspend operator fun invoke(query : CollectionQueryType) : Result<List<Item>>
}

class GetNewStoriesUseCaseImpl : GetNewStoriesUseCase, KoinComponent {

    val itemRepository : ItemRepository by inject()
    val storyRepository : StoryRepository by inject()

    override suspend fun invoke(query : CollectionQueryType): Result<List<Item>> {
        return storyRepository.getStories(query).mapCatching { stories ->
            stories.mapNotNull {
                val item = itemRepository.getItemById(it).getOrNull()
                if(item == null) {
                    Timber.w("Failed to get item $it")
                }
                return@mapNotNull item
            }
        }
    }
}