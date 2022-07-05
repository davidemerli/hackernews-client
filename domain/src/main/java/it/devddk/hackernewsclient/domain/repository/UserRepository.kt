package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.User


interface UserRepository {
    suspend fun getUserByUsername(username : String) : Result<User>
}