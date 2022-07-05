package it.devddk.hackernewsclient.data.repository.user

import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.networking.model.UserResponse
import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class UserRepositoryImpl : UserRepository, KoinComponent{

    private val userDb : DatabaseReference by inject(named("user"))

    override suspend fun getUserByUsername(username: String): Result<User> {
        return runCatching {
            val hnSnapshot = userDb.child(username).get().await()
            val user = hnSnapshot.getValue(UserResponse::class.java)?.mapToDomainModel()
            checkNotNull(user) { "Failed to retrieve user $username" }
        }
    }


}