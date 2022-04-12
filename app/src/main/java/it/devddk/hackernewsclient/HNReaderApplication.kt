package it.devddk.hackernewsclient

import android.app.Application
import android.util.Log
import it.devddk.hackernewsclient.data.BuildConfig
import it.devddk.hackernewsclient.data.di.networkingModule
import it.devddk.hackernewsclient.data.di.repositoryModule
import it.devddk.hackernewsclient.domain.di.interactionModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber


class HNReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // declare used Android context
            androidContext(this@HNReaderApplication)
            // declare modules
            modules(interactionModule, networkingModule, repositoryModule)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(Timber.DebugTree())
        }
    }


    inner class DevelopmentTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if(priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            Log.println(priority, tag, message)

            if(t != null) {
                Log.println(priority, tag, t.stackTraceToString())
            }
        }

    }

}