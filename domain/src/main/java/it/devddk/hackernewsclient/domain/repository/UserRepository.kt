package it.devddk.hackernewsclient.domain.repository


interface UserRepository {
    fun getUserByUsername(username : String) : Result<String>

    fun getModifiedProfile() : Result<List<String>>
}