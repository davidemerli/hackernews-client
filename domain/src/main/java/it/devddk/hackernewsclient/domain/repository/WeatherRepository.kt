package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.Result
import it.devddk.hackernewsclient.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun getWeatherForLocation(location: String): Result<WeatherInfo>
}
