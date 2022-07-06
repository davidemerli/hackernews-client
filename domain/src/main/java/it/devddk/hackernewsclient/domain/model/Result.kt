package it.devddk.hackernewsclient.domain.model

inline fun <T, R> Result<T>.flatMap(block: (T) -> (Result<R>)): Result<R> {
        return this.mapCatching {
            block(it).getOrThrow()
        }
    }
