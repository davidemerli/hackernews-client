package it.devddk.hackernewsclient.data.di

import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.common.utils.ConnectivityImpl
import it.devddk.hackernewsclient.data.repository.item.ItemRepositoryImpl
import it.devddk.hackernewsclient.data.repository.item.StoryRepositoryImpl
import it.devddk.hackernewsclient.data.repository.search.SearchRepositoryImpl
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import it.devddk.hackernewsclient.domain.repository.SearchRepository
import it.devddk.hackernewsclient.domain.repository.StoryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    factory<Connectivity> { ConnectivityImpl(androidContext()) }
    single<ItemRepository> { ItemRepositoryImpl() }
    single<StoryRepository> { StoryRepositoryImpl() }
    single<SearchRepository> { SearchRepositoryImpl() }
}
