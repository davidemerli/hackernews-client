package it.devddk.hackernewsclient.domain.di

import it.devddk.hackernewsclient.domain.interaction.feedback.PostFeedbackUseCase
import it.devddk.hackernewsclient.domain.interaction.feedback.PostFeedbackUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.search.SearchItemByRelevanceUseCase
import it.devddk.hackernewsclient.domain.interaction.search.SearchItemByRelevanceUseCaseImpl
import org.koin.dsl.module

val interactionModule = module {
    factory<GetItemUseCase> { GetItemUseCaseImpl() }
    factory<GetNewStoriesUseCase> { GetNewStoriesUseCaseImpl() }
    factory<SearchItemByRelevanceUseCase> { SearchItemByRelevanceUseCaseImpl() }
    factory<PostFeedbackUseCase> { PostFeedbackUseCaseImpl() }
}
