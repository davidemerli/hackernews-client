package it.devddk.hackernewsclient.data.di

import org.koin.dsl.module

val dispatcherModule = module {
    single<DispatcherProvider> {
        DispatcherProviderImpl()
    }
}