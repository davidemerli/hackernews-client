package it.devddk.hackernewsclient.domain.utils

fun <T> Sequence<T>.takeWhileInclusive(
    predicate: (T) -> Boolean
): Sequence<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = predicate(it)
        result
    }
}

inline fun <T> Iterable<T>.takeWhileInclusive(
    predicate: (T) -> Boolean
): List<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = predicate(it)
        result
    }
}