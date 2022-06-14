package it.devddk.hackernewsclient.data.networking.utils

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import kotlin.coroutines.suspendCoroutine


class HttpDownloader : KoinComponent {
    val httpClient : OkHttpClient by inject()


    private suspend fun downloadUrl(url : String) : Response {
        val request = Request.Builder().url(url).get().build()
        return suspendCoroutine { cont ->
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.isSuccessful.not()) {
                            cont.resumeWith(Result.failure(IOException("Unexpected code $response")))
                        } else {
                            cont.resumeWith(Result.success(response))
                        }
                    }
                }
            })
        }
    }

}