package it.devddk.hackernewsclient.domain.model.utils

sealed class CollectionQueryType(val entryName : String,  val maxAmount : Int)

val ALL_QUERIES = listOf(TopStories, NewStories, JobStories, ShowStories, BestStories, AskStories)

object NewStories : CollectionQueryType("New Stories", 500)
object TopStories : CollectionQueryType("Top Stories", 500)
object BestStories : CollectionQueryType("Best Stories", 500)
object AskStories : CollectionQueryType("Ask HN", 200)
object ShowStories : CollectionQueryType("Show HN",200)
object JobStories : CollectionQueryType("HN Jobs", 200)

