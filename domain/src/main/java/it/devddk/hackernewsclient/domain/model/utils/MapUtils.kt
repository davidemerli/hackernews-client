package it.devddk.hackernewsclient.domain.model.utils

import java.lang.Exception

suspend fun <Id, Expanded> Map<Id, Expanded>.expandSafe(
    expander: suspend (Id) -> Expanded?,
    onException: (Id, Exception) -> Expanded? = { _, _ -> null }
): Map<Id, Expanded?> {
    return this.mapValues { (id, value) ->
        return@mapValues try {
            value ?: expander(id)
        } catch (e: Exception) {
            onException(id, e)
        }
    }
}