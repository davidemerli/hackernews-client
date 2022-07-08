package it.devddk.hackernewsclient.data.api

import it.devddk.hackernewsclient.domain.model.search.NumericalSearchFilters
import it.devddk.hackernewsclient.domain.model.search.SearchTags
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class AlgoliaApiConverterFactory : Converter.Factory() {

        override fun stringConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<*, String>? {
            return when {
                SearchTags::class.java.isAssignableFrom(type as Class<*>) -> Converter<SearchTags, String> {
                    it.string
                }
                NumericalSearchFilters::class.java.isAssignableFrom(type as Class<*>) -> Converter<NumericalSearchFilters, String> {
                    it.toString()
                }
                else -> super.stringConverter(type, annotations, retrofit)
        }
    }
}