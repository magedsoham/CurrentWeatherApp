package com.maged.currentweatherapp.data.repositories

import com.maged.currentweatherapp.data.local.WeatherDatabase
import com.maged.currentweatherapp.data.model.WeatherDataResponse
import com.maged.currentweatherapp.data.model.WeatherDetail
import com.maged.currentweatherapp.data.network.ApiInterface
import com.maged.currentweatherapp.data.network.SafeApiRequest

class WeatherRepository(
    private val api: ApiInterface,
    private val db: WeatherDatabase
) : SafeApiRequest() {

    suspend fun findCityWeather(password:String,cityName: String): WeatherDataResponse = apiRequest {
        api.findCityWeatherData(cityName,password)
    }

    suspend fun addWeather(weatherDetail: WeatherDetail) {
        db.getWeatherDao().addWeather(weatherDetail)
    }

    suspend fun fetchWeatherDetail(cityName: String): WeatherDetail? =
        db.getWeatherDao().fetchWeatherByCity(cityName)

    suspend fun fetchAllWeatherDetails(): List<WeatherDetail> =
        db.getWeatherDao().fetchAllWeatherDetails()
}
