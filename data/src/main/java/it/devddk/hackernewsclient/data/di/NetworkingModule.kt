package it.devddk.hackernewsclient.data.di

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import it.devddk.hackernewsclient.data.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


val networkingModule = module {
    single { GsonConverterFactory.create() as Converter.Factory }
    single { HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY) }
    single {
        OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) addInterceptor { get() }
                .callTimeout(10, TimeUnit.SECONDS)
        }.build()
    }
    single {
        Firebase.database("https://hacker-news.firebaseio.com/")
    }
    single(named("item")) {
        get<FirebaseDatabase>().getReference("v0/item")
    }
    single(named("root")) {
        get<FirebaseDatabase>().getReference("v0")
    }
}
