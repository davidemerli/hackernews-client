package it.devddk.hackernewsclient.data.di

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import it.devddk.hackernewsclient.data.BuildConfig
import it.devddk.hackernewsclient.data.api.AlgoliaApiConverterFactory
import it.devddk.hackernewsclient.data.api.AlgoliaSearchApi
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.common.utils.ConnectivityImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


val networkingModule = module {
    single { GsonConverterFactory.create() as Converter.Factory }
    single { HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY) }
    single {
        OkHttpClient.Builder().build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.ALGOLIA_URL)
            .client(get())
            .addConverterFactory(AlgoliaApiConverterFactory())
            .addConverterFactory(get())
            .build()
    }
    single {
        get<Retrofit>().create(AlgoliaSearchApi::class.java)
    }
    single(named("HN")) {
        Firebase.database("https://hacker-news.firebaseio.com/").apply { setPersistenceEnabled(true) }
    }
    single(named("ourDb")) {
        Firebase.database("https://hackernews-client-82b42-default-rtdb.europe-west1.firebasedatabase.app/")
    }
    single(named("item")) {
        get<FirebaseDatabase>(named("HN")).getReference("v0/item")
    }
    single(named("user")) {
        get<FirebaseDatabase>(named("HN")).getReference("v0/user")
    }
    single(named("root")) {
        get<FirebaseDatabase>(named("HN")).getReference("v0")
    }
    single(named("feedback")) {
        get<FirebaseDatabase>(named("ourDb")).getReference("feedback")
    }
    single<Connectivity> {
        ConnectivityImpl(androidContext())
    }
}
