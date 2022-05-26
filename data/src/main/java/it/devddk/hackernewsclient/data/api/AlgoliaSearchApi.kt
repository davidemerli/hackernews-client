package it.devddk.hackernewsclient.data.api

import it.devddk.hackernewsclient.data.networking.model.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AlgoliaSearchApi {

    @GET("search")
    suspend fun searchByRelevance(@Query("query") query : String, @Query("page") page : String) : Response<SearchResponse>

}