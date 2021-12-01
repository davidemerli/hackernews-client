package it.devddk.hackernewsclient.domain.base

interface BaseUseCase<T : Any, R : Any> {
    // TODO: check if 'param' can be called with a different name,
    //  AS complaints about it being a problem for named parameters calls
    suspend operator fun invoke(param: T): Result<R>
}
