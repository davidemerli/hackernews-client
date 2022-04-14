package it.devddk.hackernewsclient.domain.utils

fun <T> Result.Companion.skipped() : Result<T> {
    return Result.failure(ResultSkipped())
}

val <T> Result<T>.isSkipped : Boolean
    get() = fold({_ -> false}, {it is ResultSkipped})

fun <T> Result<T>.onSkipped(f: () -> Unit) {
    if(isSkipped) {
        f()
    }
}

fun <T, R> Result<T>.foldSkipped(onSuccess : (T) -> R, onSkipped : () -> R, onFailure : (Throwable) -> R) : R {
    return fold(onSuccess) {
        if(isSkipped) {
            onSkipped()
        } else {
            onFailure(it)
        }
    }
}