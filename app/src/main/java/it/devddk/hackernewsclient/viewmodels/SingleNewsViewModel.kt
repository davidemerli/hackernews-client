package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception

class SingleNewsViewModel : ViewModel(), KoinComponent {

    private val getItemById: GetItemUseCase by inject()

    private val _uiState = MutableStateFlow(SingleNewsUiState.Loading as SingleNewsUiState)
    val uiState = _uiState.asStateFlow()

    suspend fun setId(newId: Int?) {
        _uiState.emit(SingleNewsUiState.Loading)
        if (newId == null) {
            _uiState.emit(SingleNewsUiState.Error(Exception("EEE")))
            return
        }
        withContext(Dispatchers.IO) {
            getItemById(newId).fold(
                onSuccess = {
                    _uiState.emit(SingleNewsUiState.ItemLoaded(it))
                },
                onFailure = {
                    _uiState.emit(SingleNewsUiState.Error(it))
                }
            )
        }
    }
}


sealed class SingleNewsUiState {
    object Loading : SingleNewsUiState()
    data class Error(val throwable: Throwable) : SingleNewsUiState()
    data class ItemLoaded(val item: Item) : SingleNewsUiState()
}