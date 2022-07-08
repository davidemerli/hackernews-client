package it.devddk.hackernewsclient.data.di

import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.common.utils.ConnectivityImpl
import it.devddk.hackernewsclient.data.repository.feedback.FeedbackRepositoryImpl
import it.devddk.hackernewsclient.data.repository.item.ItemRepositoryImpl
import it.devddk.hackernewsclient.data.repository.item.HNCollectionRepositoryImpl
import it.devddk.hackernewsclient.data.repository.search.SearchRepositoryImpl
import it.devddk.hackernewsclient.domain.repository.FeedbackRepository
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import it.devddk.hackernewsclient.domain.repository.SearchRepository
import it.devddk.hackernewsclient.domain.repository.HNCollectionRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    factory<Connectivity> { ConnectivityImpl(androidContext()) }
    single<ItemRepository> { ItemRepositoryImpl() }
    single<HNCollectionRepository> { HNCollectionRepositoryImpl() }
    single<SearchRepository> { SearchRepositoryImpl() }
    single<FeedbackRepository> { FeedbackRepositoryImpl() }
}
