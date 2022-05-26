package it.devddk.hackernewsclient.data.networking.base

import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

interface DomainMapper<T : Any> {
    fun mapToDomainModel(): T
}

interface RoomMapper<out T : Any> {
    fun mapToRoomEntity(): T
}
/*

/**
 * Use this if you need to cache data after fetching it from the api, or retrieve something from cache
 */

inline fun <T : RoomMapper<R>, R : DomainMapper<U>, U : Any> Response<T>.getData(
    cacheAction: (R) -> Unit,
    fetchFromCacheAction: () -> R,
): Result<U> {
    try {
        onSuccess {
            val databaseEntity = it.mapToRoomEntity()
            cacheAction(databaseEntity)
            return Success(databaseEntity.mapToDomainModel())
        }
        onFailure {
            val cachedModel = fetchFromCacheAction()
            if (cachedModel != null) {
                Success(cachedModel.mapToDomainModel())
            } else {
                Failure(HttpError(Throwable(DB_ENTRY_ERROR)))
            }
        }
        return Failure(HttpError(Throwable(GENERAL_NETWORK_ERROR)))
    } catch (e: IOException) {
        return Failure(HttpError(Throwable(GENERAL_NETWORK_ERROR)))
    }
}
*/
/**
 * Use this when communicating only with the api service
 */
fun <T : DomainMapper<R>, R : Any> Response<T>.getData(): Result<R> {
    return try {
        if(isSuccessful) {
            Result.success(this.body()?.mapToDomainModel()!!)
        } else {
            Result.failure(HttpException(this))
        }
    } catch (e: IOException) {
        Result.failure(HttpException(this))
    }
}

/**
 * Use this when communicating only with the api service
 */
fun <R : Any> Response<R>.asResult(): Result<R> {
    return try {
        if(isSuccessful) {
            Result.success(this.body()!!)
        } else {
            Result.failure(HttpException(this))
        }
    } catch (e: IOException) {
        Result.failure(HttpException(this))
    }
}
