package com.maged.currentweatherapp

import android.app.Application
import com.maged.currentweatherapp.data.local.WeatherDatabase
import com.maged.currentweatherapp.data.network.ApiInterface
import com.maged.currentweatherapp.data.network.NetworkConnectionInterceptor
import com.maged.currentweatherapp.data.repositories.WeatherRepository
import com.maged.currentweatherapp.viewmodelfactory.WeatherViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class WeatherApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@WeatherApplication))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { ApiInterface(instance()) }
        bind() from singleton { WeatherRepository(instance(), instance()) }
        bind() from provider { WeatherViewModelFactory(instance()) }
        bind() from provider { WeatherDatabase(instance()) }
    }


}
