package it.devddk.hackernewsclient.data.common.utils

import android.content.Context
import android.net.ConnectivityManager

class ConnectivityImpl(private val context: Context) : Connectivity {

    override fun hasNetworkAccess(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // TODO: fix deprecated fields
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnected
    }
}
