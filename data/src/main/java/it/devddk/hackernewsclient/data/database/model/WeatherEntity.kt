package it.devddk.hackernewsclient.data.database.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import it.devddk.hackernewsclient.data.database.WEATHER_TABLE_NAME
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.data.networking.model.MainInfo
import it.devddk.hackernewsclient.data.networking.model.Weather
import it.devddk.hackernewsclient.domain.model.WeatherInfo

@Entity(tableName = WEATHER_TABLE_NAME)
data class WeatherEntity(
    @PrimaryKey val id: Int? = 0,
    val weather: List<Weather>?,
    @Embedded
    val main: MainInfo?,
    val name: String? = "",
) : DomainMapper<WeatherInfo> {

    override fun mapToDomainModel() =
        WeatherInfo(main?.temp ?: 0.0, main?.humidity ?: 0, main?.pressure ?: 0.0)
}
