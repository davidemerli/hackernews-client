package it.devddk.hackernewsclient.domain.di

import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCaseImpl
import org.koin.dsl.module

val interactionModule = module {
    factory<GetItemUseCase> { GetItemUseCaseImpl() }
    factory<GetNewStoriesUseCase> {GetNewStoriesUseCaseImpl()}
}
