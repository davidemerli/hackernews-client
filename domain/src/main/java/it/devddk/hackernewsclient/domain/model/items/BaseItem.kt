package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import java.time.LocalDateTime

interface BaseItem{
    val id: Int
    val type: ItemType
    val deleted: Boolean
    val by: User?
    val time: LocalDateTime
    val dead: Boolean
    val kids: List<BaseItem>?

}

