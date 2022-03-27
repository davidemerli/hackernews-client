package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.User

interface UserRepository {
    fun getUserByUsername(username : List<String>) : Result<User>

    fun getModifiedProfile() : Result<List<User>>
}