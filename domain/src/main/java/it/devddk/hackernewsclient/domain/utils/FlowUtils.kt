package it.devddk.hackernewsclient.domain.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

fun <T> SharedFlow<T>.requireValue() : T {
    return replayCache[0]
}

fun <T> SharedFlow<T>.getValue() : T? {
    return replayCache.getOrNull(0)
}