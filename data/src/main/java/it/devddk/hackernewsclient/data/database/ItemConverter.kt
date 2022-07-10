package it.devddk.hackernewsclient.data.database

import android.database.Cursor
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.networking.utils.toLocalDateTime
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import java.time.LocalDateTime
import java.time.ZoneId

class ItemConverter {

    @TypeConverter
    fun fromItemIdList(value: List<Int>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toItemIdList(value: String): List<Int> {
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.toLocalDateTime()
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?) : Long? {
        return value?.atZone(ZoneId.systemDefault())?.toEpochSecond()
    }

    @TypeConverter
    fun toItemCollectionName(name: String) : UserDefinedItemCollection {
        return UserDefinedItemCollection.valueOf(name)!!
    }

    @TypeConverter
    fun toItemCollectionName(x: UserDefinedItemCollection) : String {
        return x::class.java.simpleName!!
    }
}