package it.devddk.hackernewsclient.domain.model.utils

sealed class CollectionQueryType(val maxAmount : Int)

val ALL_QUERIES = listOf(TopStories, NewStories, JobStories, ShowStories, BestStories, AskStories)

object NewStories : CollectionQueryType(500)
object TopStories : CollectionQueryType(500)
object BestStories : CollectionQueryType(500)
object AskStories : CollectionQueryType(200)
object ShowStories : CollectionQueryType(200)
object JobStories : CollectionQueryType(200)

