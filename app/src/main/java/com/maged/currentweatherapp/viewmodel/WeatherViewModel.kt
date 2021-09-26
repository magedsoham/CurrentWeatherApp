package com.maged.currentweatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maged.currentweatherapp.data.model.WeatherDataResponse
import com.maged.currentweatherapp.data.model.WeatherDetail
import com.maged.currentweatherapp.data.repositories.WeatherRepository
import com.maged.currentweatherapp.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class WeatherViewModel(private val repository: WeatherRepository) :
    ViewModel() {

    private val _weatherLiveData =
        MutableLiveData<Event<State<WeatherDetail>>>()
    val weatherLiveData: LiveData<Event<State<WeatherDetail>>>
        get() = _weatherLiveData

    private val _weatherDetailListLiveData =
        MutableLiveData<Event<State<List<WeatherDetail>>>>()
    val weatherDetailListLiveData: LiveData<Event<State<List<WeatherDetail>>>>
        get() = _weatherDetailListLiveData

    private lateinit var weatherResponse: WeatherDataResponse

    private fun findCityWeather(password: String,cityName: String) {
        _weatherLiveData.postValue(Event(State.loading()))
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherResponse =
                    repository.findCityWeather(password,cityName)
                addWeatherDetailIntoDb(weatherResponse)
                withContext(Dispatchers.Main) {
                    val weatherDetail = WeatherDetail()
                    weatherDetail.icon = weatherResponse.weather.first().icon
                    weatherDetail.cityName = weatherResponse.name
                    weatherDetail.countryName = weatherResponse.sys.country
                    weatherDetail.temp = weatherResponse.main.temp
                    weatherDetail.humidity = weatherResponse.main.humidity
                    weatherDetail.tempMin = weatherResponse.main.tempMin
                    weatherDetail.tempMax = weatherResponse.main.tempMax
                    weatherDetail.dateTime = AppUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_1)
                    weatherDetail.dt = weatherResponse.dt
                    weatherDetail.name = weatherResponse.name
                    weatherDetail.sunrise = weatherResponse.sys.sunrise
                    weatherDetail.sunset = weatherResponse.sys.sunset
                    weatherDetail.description = weatherResponse.weather.first().description
                    weatherDetail.speed = weatherResponse.wind.speed
                    weatherDetail.main = weatherResponse.weather.first().main
                    _weatherLiveData.postValue(
                        Event(
                            State.success(
                                weatherDetail
                            )
                        )
                    )
                }
            } catch (e: ApiException) {
                withContext(Dispatchers.Main) {
                    _weatherLiveData.postValue(Event(State.error(e.message ?: "")))
                }
            } catch (e: NoInternetException) {
                withContext(Dispatchers.Main) {
                    _weatherLiveData.postValue(Event(State.error(e.message ?: "")))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _weatherLiveData.postValue(
                        Event(
                            State.error(
                                e.message ?: ""
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun addWeatherDetailIntoDb(weatherResponse: WeatherDataResponse) {
        val weatherDetail = WeatherDetail()
        weatherDetail.id = weatherResponse.id
        weatherDetail.icon = weatherResponse.weather.first().icon
        weatherDetail.cityName = weatherResponse.name.lowercase(Locale.getDefault())
        weatherDetail.countryName = weatherResponse.sys.country
        weatherDetail.temp = weatherResponse.main.temp
        weatherDetail.humidity = weatherResponse.main.humidity
        weatherDetail.tempMin = weatherResponse.main.tempMin
        weatherDetail.tempMax = weatherResponse.main.tempMax
        weatherDetail.dateTime = AppUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_1)
        weatherDetail.dt = weatherResponse.dt
        weatherDetail.name = weatherResponse.name
        weatherDetail.sunrise = weatherResponse.sys.sunrise
        weatherDetail.sunset = weatherResponse.sys.sunset
        weatherDetail.description = weatherResponse.weather.first().description
        weatherDetail.speed = weatherResponse.wind.speed
        weatherDetail.main = weatherResponse.weather.first().main
        repository.addWeather(weatherDetail)
    }

    fun fetchWeatherDetailFromDb(password:String,cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val weatherDetail =
                repository.fetchWeatherDetail(cityName.lowercase(Locale.getDefault()))
            withContext(Dispatchers.Main) {
                if (weatherDetail != null) {
                    // Return true of current date and time is greater then the saved date and time of weather searched
                    if (AppUtils.isTimeExpired(weatherDetail.dateTime)) {
                        findCityWeather(password, cityName)
                    } else {
                        _weatherLiveData.postValue(
                            Event(
                                State.success(
                                    weatherDetail
                                )
                            )
                        )
                    }

                } else {
                    findCityWeather(password, cityName)
                }

            }
        }
    }

    fun fetchAllWeatherDetailsFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val weatherDetailList = repository.fetchAllWeatherDetails()
            withContext(Dispatchers.Main) {
                _weatherDetailListLiveData.postValue(
                    Event(
                        State.success(weatherDetailList)
                    )
                )
            }
        }
    }
}
