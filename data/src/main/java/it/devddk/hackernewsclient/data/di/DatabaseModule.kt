package it.devddk.hackernewsclient.data.di

import androidx.room.Room
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        //TODO remove fallbackToDestructiveMigration when this goes to production
        Room.databaseBuilder(androidContext(), LocalDatabase::class.java, "local")
            .fallbackToDestructiveMigration().build()
    }

    factory {
        get<LocalDatabase>().itemCollectionDao()
    }

    factory {
        get<LocalDatabase>().saveItemDao()
    }

    factory {
        get<LocalDatabase>().itemEntityDao()
    }
}
