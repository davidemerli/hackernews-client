package it.devddk.hackernewsclient.utils

// json
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

// decode json
fun <T> String.decodeJson(clazz: Class<T>): T = Gson().fromJson(this, clazz)

// encode json
fun <T> T.encodeJson(): String = Gson().toJson(this)

// url encode
fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

// url decode
fun String.urlDecode(): String = URLDecoder.decode(this, "UTF-8")
