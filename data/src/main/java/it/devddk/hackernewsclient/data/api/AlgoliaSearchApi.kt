package it.devddk.hackernewsclient.data.api

import it.devddk.hackernewsclient.data.networking.model.AlgoliaItemResponse
import it.devddk.hackernewsclient.data.networking.model.SearchResponse
import it.devddk.hackernewsclient.domain.model.search.NumericalSearchFilters
import it.devddk.hackernewsclient.domain.model.search.SearchTags
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AlgoliaSearchApi {

    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: Int) : Response<AlgoliaItemResponse>

    @GET("search")
    suspend fun searchByRelevance(@Query("query") query : String? = null,  @Query("tags") tags : SearchTags? = null, @Query("numericFilters") numericFilters : NumericalSearchFilters? = null,  @Query("page") page : Int) : Response<SearchResponse>

    @GET("search_by_date")
    suspend fun searchByTime(@Query("query") query : String? = null, @Query("tags") tags : SearchTags? = null, @Query("numericFilters") numericFilters : NumericalSearchFilters? = null, @Query("page") page : Int) : Response<SearchResponse>

}