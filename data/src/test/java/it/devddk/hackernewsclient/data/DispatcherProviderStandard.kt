package it.devddk.hackernewsclient.data

import it.devddk.hackernewsclient.data.di.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

class DispatcherProviderTesting(
    override val Default: CoroutineDispatcher,
    override val IO: CoroutineDispatcher,
    override val Main: CoroutineDispatcher,
    override val Unconfined: CoroutineDispatcher
) : DispatcherProvider