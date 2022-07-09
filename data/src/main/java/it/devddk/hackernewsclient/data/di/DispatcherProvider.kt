package it.devddk.hackernewsclient.data.di

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val Default: CoroutineDispatcher
    val IO: CoroutineDispatcher
    val Main: CoroutineDispatcher
    val Unconfined: CoroutineDispatcher
}