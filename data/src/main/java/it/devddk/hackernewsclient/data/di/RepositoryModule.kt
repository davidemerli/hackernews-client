package it.devddk.hackernewsclient.data.di

import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.common.utils.ConnectivityImpl
import it.devddk.hackernewsclient.data.repository.weather.WeatherRepositoryImpl
import it.devddk.hackernewsclient.domain.repository.WeatherRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    factory<WeatherRepository> { WeatherRepositoryImpl(get(), get()) }
    factory<Connectivity> { ConnectivityImpl(androidContext()) }
}
