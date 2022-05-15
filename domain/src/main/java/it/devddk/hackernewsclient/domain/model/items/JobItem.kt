package it.devddk.hackernewsclient.domain.model.items

import java.time.LocalDateTime

interface IJobItem : IBaseItem {
    val title: String?
    val score: Int?
    val url: String?
}

data class JobItem(
    override val id: Int,
    override val deleted: Boolean = false,
    override val by: String?,
    override val time: LocalDateTime?,
    override val dead: Boolean = false,
    override val title: String?,
    override val score: Int?,
    override val url : String?
) : IJobItem {
    override val type = ItemType.JOB
}