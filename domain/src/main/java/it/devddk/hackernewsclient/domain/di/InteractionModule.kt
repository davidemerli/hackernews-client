package it.devddk.hackernewsclient.domain.di

import it.devddk.hackernewsclient.domain.interaction.collection.AddItemToCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.collection.AddItemToCollectionUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.collection.GetCollectionsUseCase
import it.devddk.hackernewsclient.domain.interaction.collection.GetCollectionsUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.collection.RemoveItemFromCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.collection.RemoveItemFromCollectionUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.feedback.PostFeedbackUseCase
import it.devddk.hackernewsclient.domain.interaction.feedback.PostFeedbackUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.item.RefreshAllItemsUseCase
import it.devddk.hackernewsclient.domain.interaction.item.RefreshAllItemsUseCaseImpl
import it.devddk.hackernewsclient.domain.interaction.search.SearchItemByRelevanceUseCase
import it.devddk.hackernewsclient.domain.interaction.search.SearchItemByRelevanceUseCaseImpl
import org.koin.dsl.module

val interactionModule = module {
    factory<GetItemUseCase> { GetItemUseCaseImpl() }
    factory<GetNewStoriesUseCase> { GetNewStoriesUseCaseImpl() }
    factory<SearchItemByRelevanceUseCase> { SearchItemByRelevanceUseCaseImpl() }
    factory<PostFeedbackUseCase> { PostFeedbackUseCaseImpl() }
    factory<AddItemToCollectionUseCase> { AddItemToCollectionUseCaseImpl() }
    factory<RemoveItemFromCollectionUseCase> { RemoveItemFromCollectionUseCaseImpl() }
    factory<GetCollectionsUseCase> { GetCollectionsUseCaseImpl() }
    factory<RefreshAllItemsUseCase> { RefreshAllItemsUseCaseImpl() }
}
