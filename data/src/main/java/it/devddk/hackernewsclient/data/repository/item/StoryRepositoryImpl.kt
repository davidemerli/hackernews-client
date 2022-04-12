package it.devddk.hackernewsclient.data.repository.item

import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.domain.model.utils.*
import it.devddk.hackernewsclient.domain.repository.StoryRepository
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class StoryRepositoryImpl : StoryRepository, KoinComponent {

    val dbRef: DatabaseReference by inject()

    override suspend fun getStories(query: CollectionQueryType): Result<List<ItemId>> {
        return runCatching {
            val what = when (query) {
                AskStories -> "topstories"
                BestStories -> "beststories"
                JobStories -> "jobstories"
                NewStories -> "newstories"
                ShowStories -> "showstories"
                TopStories -> "topstories"
            }
            val value = dbRef.child(what).get().await().getValue(List::class.java) as List<ItemId>?
            checkNotNull(value) {
                Timber.e("Story repository returned null for query $query")
                "Story repository returned null for query $query"
            }
        }
    }
}