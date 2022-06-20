package it.devddk.hackernewsclient.data.repository.item

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import it.devddk.hackernewsclient.domain.model.collection.AskStories
import it.devddk.hackernewsclient.domain.model.collection.BestStories
import it.devddk.hackernewsclient.domain.model.collection.HNItemCollection
import it.devddk.hackernewsclient.domain.model.collection.JobStories
import it.devddk.hackernewsclient.domain.model.collection.NewStories
import it.devddk.hackernewsclient.domain.model.collection.ShowStories
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.utils.*
import it.devddk.hackernewsclient.domain.repository.HNCollectionRepository
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class HNCollectionRepositoryImpl : HNCollectionRepository, KoinComponent {


    val root: DatabaseReference by inject(named("root"))

    override suspend fun getStories(query: HNItemCollection): Result<List<ItemId>> {
        return runCatching {
            val what = when (query) {
                AskStories -> "askstories"
                BestStories -> "beststories"
                JobStories -> "jobstories"
                NewStories -> "newstories"
                ShowStories -> "showstories"
                TopStories -> "topstories"
            }
            val value = root.child(what).get().await()
                .getValue(object : GenericTypeIndicator<List<ItemId>>() {})
            checkNotNull(value) {
                Timber.e("Story repository returned null for query $query")
                "Story repository returned null for query $query"
            }
        }
    }
}