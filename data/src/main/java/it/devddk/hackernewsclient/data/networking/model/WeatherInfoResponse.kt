package it.devddk.hackernewsclient.data.networking.model

import it.devddk.hackernewsclient.data.database.model.WeatherEntity
import it.devddk.hackernewsclient.data.networking.base.RoomMapper

data class WeatherInfoResponse(
    val id: Int? = 0,
    val weather: List<Weather>?,
    val main: MainInfo?,
    val name: String? = "",
) : RoomMapper<WeatherEntity> {

    override fun mapToRoomEntity() =
        WeatherEntity(id, weather ?: arrayListOf(), main ?: MainInfo(), name ?: "")
}

data class MainInfo(
    val temp: Double? = 0.0,
    val pressure: Double? = 0.0,
    val humidity: Int? = 0,
)

data class Weather(
    val id: Int? = 0,
    val main: String? = "",
    val description: String? = "",
    val icon: String? = "",
)
