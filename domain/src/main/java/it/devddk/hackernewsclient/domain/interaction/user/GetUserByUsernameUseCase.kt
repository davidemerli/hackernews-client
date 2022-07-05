package it.devddk.hackernewsclient.domain.interaction.user

import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.repository.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetUserByUsernameUseCase {
    suspend operator fun invoke(username: String): Result<User>
}

class GetUserByUsernameUseCaseImpl : GetUserByUsernameUseCase, KoinComponent {

    private val userRepository: UserRepository by inject()

    override suspend operator fun invoke(username: String): Result<User> {
        return userRepository.getUserByUsername(username)
    }
}