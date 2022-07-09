package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import it.devddk.hackernewsclient.domain.interaction.user.GetUserByUsernameUseCase
import it.devddk.hackernewsclient.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserViewModel : ViewModel(), KoinComponent {

    private val _uiState = MutableStateFlow<UserUIState>(UserUIState.Loading(""))
    val uiState = _uiState.asStateFlow()
    private val getUserByUsername: GetUserByUsernameUseCase by inject()

    suspend fun requestUser(username: String) {
        _uiState.emit(UserUIState.Loading(username))
        getUserByUsername(username).fold(
            onSuccess = {
                _uiState.emit(UserUIState.UserLoaded(username, it))
            },
            onFailure = {
                _uiState.emit(UserUIState.Error(username, it))
            }
        )
    }

    suspend fun reloadUser() {
        requestUser(_uiState.value.username)
    }
}

sealed class UserUIState(open val username: String) {
    data class Loading(override val username: String) : UserUIState(username)
    data class UserLoaded(override val username: String, val userData: User) : UserUIState(username)
    data class Error(override val username: String, val error: Throwable) : UserUIState(username)
}
