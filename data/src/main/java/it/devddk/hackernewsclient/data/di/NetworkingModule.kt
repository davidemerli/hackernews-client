package it.devddk.hackernewsclient.data.di

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import it.devddk.hackernewsclient.data.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
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
        FirebaseApp.initializeApp(androidContext())
        val options = FirebaseApp.getInstance().options
        print(options)
        FirebaseOptions.Builder(options).setDatabaseUrl("hacker-news").build()
        System.out.println("peppe")
        val second = FirebaseApp.initializeApp(androidContext(), options, "aaaa")
        val secondDb = FirebaseDatabase.getInstance(second)
        secondDb.setPersistenceEnabled(true)
        return@single secondDb.reference
    }
}
