package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.NewStories
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomePageViewModel : ViewModel(), KoinComponent {

    val getNewStories: GetNewStoriesUseCase by inject()

    private val _articles = MutableLiveData<List<Item>>(listOf())

    val _success = MutableLiveData(false)

    val success: LiveData<Boolean> = _success

    val articles: LiveData<List<Item>> = _articles


    fun requestArticles() {
        viewModelScope.launch {
            val result = getNewStories(NewStories, 10)
            _success.postValue(result.isSuccess)
            result.fold(
                onSuccess = { items ->
                    _articles.postValue(
                        items.mapNotNull { (_, item) -> item.getOrNull() }
                    )
                },
                onFailure = {}
            )
        }
    }
}