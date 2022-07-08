package it.devddk.hackernewsclient.data.api

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
                AlgoliaTags::class.java.isAssignableFrom(type as Class<*>) -> Converter<AlgoliaTags, String> {
                    it.string
                }
                else -> super.stringConverter(type, annotations, retrofit)
        }
    }
}