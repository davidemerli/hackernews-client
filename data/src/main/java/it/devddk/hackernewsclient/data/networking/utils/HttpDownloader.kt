package it.devddk.hackernewsclient.data.networking.utils

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.suspendCoroutine


suspend fun getUrl(httpClient: OkHttpClient, request: Request): Response {
    return suspendCoroutine { cont ->
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWith(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful.not()) {
                    cont.resumeWith(Result.failure(IOException("Unexpected code $response")))
                } else {
                    cont.resumeWith(Result.success(response))
                }
            }
        })
    }
}

suspend fun getBody(httpClient: OkHttpClient, request: Request): String {
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
                        cont.resumeWith(response.body?.let {
                            Result.success(it.string())
                        } ?: Result.failure(Exception("Null body")))
                    }
                }
            }
        })
    }
}
