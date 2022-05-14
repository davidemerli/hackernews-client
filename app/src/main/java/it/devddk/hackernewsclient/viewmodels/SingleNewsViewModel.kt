package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SingleNewsViewModel : ViewModel(), KoinComponent {

    val getItemById: GetItemUseCase by inject()

    private val _id = MutableStateFlow(null as ItemId?)
    val id = _id.asStateFlow()
    val itemResult = id.map { it?.let { getItemById(it) } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}