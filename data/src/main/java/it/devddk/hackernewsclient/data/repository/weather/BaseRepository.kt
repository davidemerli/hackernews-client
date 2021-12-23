package it.devddk.hackernewsclient.data.repository.weather

import it.devddk.hackernewsclient.data.common.coroutine.CoroutineContextProvider
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.database.DB_ENTRY_ERROR
import it.devddk.hackernewsclient.data.networking.GENERAL_NETWORK_ERROR
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.domain.model.Failure
import it.devddk.hackernewsclient.domain.model.HttpError
import it.devddk.hackernewsclient.domain.model.Result
import it.devddk.hackernewsclient.domain.model.Success
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseRepository<T : Any, R : DomainMapper<T>> : KoinComponent {
    private val connectivity: Connectivity by inject()
    private val contextProvider: CoroutineContextProvider by inject()

    /**
     * Use this if you need to cache data after fetching it from the api, or retrieve something from cache
     */
    protected suspend fun fetchData(
        apiDataProvider: suspend () -> Result<T>,
        dbDataProvider: suspend () -> R,
    ): Result<T> {
        return if (connectivity.hasNetworkAccess()) {
            withContext(contextProvider.io) {
                apiDataProvider()
            }
        } else {
            withContext(contextProvider.io) {
                val dbResult = dbDataProvider()

                if (dbResult != null) {
                    Success(dbResult.mapToDomainModel())
                } else {
                    Failure(HttpError(Throwable(DB_ENTRY_ERROR)))
                }
            }
        }
    }

    /**
     * Use this when communicating only with the api service
     */
    protected suspend fun fetchData(dataProvider: () -> Result<T>): Result<T> {
        return if (connectivity.hasNetworkAccess()) {
            withContext(contextProvider.io) {
                dataProvider()
            }
        } else {
            Failure(HttpError(Throwable(GENERAL_NETWORK_ERROR)))
        }
    }
}
