package com.maged.currentweatherapp.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maged.currentweatherapp.data.repositories.WeatherRepository
import com.maged.currentweatherapp.viewmodel.WeatherViewModel

@Suppress("UNCHECKED_CAST")
class WeatherViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherViewModel(repository) as T
    }
}
