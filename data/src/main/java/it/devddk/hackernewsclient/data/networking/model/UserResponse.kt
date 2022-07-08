package it.devddk.hackernewsclient.data.networking.model

import it.devddk.hackernewsclient.data.networking.DomainMapper
import it.devddk.hackernewsclient.data.networking.utils.toLocalDateTime
import it.devddk.hackernewsclient.domain.model.User

data class UserResponse(
    val id: String? = null,
    val created: Long? = null,
    val karma: Int? = null,
    val about: String? = null,
    val submitted: List<Int> = emptyList()
) : DomainMapper<User> {
    override fun mapToDomainModel(): User {
        return User(
            id!!,
            created?.toLocalDateTime(),
            karma,
            about,
            submitted
        )
    }
}