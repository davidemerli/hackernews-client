package it.devddk.hackernewsclient.domain.di

import it.devddk.hackernewsclient.domain.interaction.news.GetNewsUseCase
import it.devddk.hackernewsclient.domain.interaction.news.GetNewsUseCaseImpl
import org.koin.dsl.module

val interactionModule = module {
    factory<GetNewsUseCase> { GetNewsUseCaseImpl(get()) }
}
