package it.devddk.hackernewsclient.data.repository.weather

import it.devddk.hackernewsclient.data.database.dao.WeatherDao
import it.devddk.hackernewsclient.data.database.model.WeatherEntity
import it.devddk.hackernewsclient.data.networking.WeatherApi
import it.devddk.hackernewsclient.data.networking.base.getData
import it.devddk.hackernewsclient.domain.model.Result
import it.devddk.hackernewsclient.domain.model.WeatherInfo
import it.devddk.hackernewsclient.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao,
) : BaseRepository<WeatherInfo, WeatherEntity>(), WeatherRepository {
    override suspend fun getWeatherForLocation(location: String): Result<WeatherInfo> {
        return fetchData(
            apiDataProvider = {
                weatherApi.getWeatherForLocation(location).getData(
                    fetchFromCacheAction = { weatherDao.getWeatherInfoForCity(location) },
                    cacheAction = { weatherDao.saveWeatherInfo(it) })
            },
            dbDataProvider = { weatherDao.getWeatherInfoForCity(location) }
        )
    }
}
