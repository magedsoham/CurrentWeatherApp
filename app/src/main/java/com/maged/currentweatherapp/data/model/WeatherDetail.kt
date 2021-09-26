package com.maged.currentweatherapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maged.currentweatherapp.data.model.WeatherDetail.Companion.TABLE_NAME

/**
 * Data class for Database entity and Serialization.
 */
@Entity(tableName = TABLE_NAME)
data class WeatherDetail(

    @PrimaryKey
    var id: Int? = 0,
    var temp: Double? = null,
    var humidity: Int? = null,
    var tempMin: Double? = null,
    var tempMax: Double? = null,
    var icon: String? = null,
    var cityName: String? = null,
    var countryName: String? = null,
    var dateTime: String? = null,
    var dt: Int? = null,
    var name: String? = null,
    var sunrise: Int? = null,
    var sunset: Int? = null,
    var description: String? = null,
    var speed: Double? = null,
    var main: String? = null
) {

    companion object {
        const val TABLE_NAME = "weather_detail"
    }
}