package com.maged.currentweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.maged.currentweatherapp.GPSTracker.Companion.REQUEST_CHECK_GPS
import com.maged.currentweatherapp.GPSTracker.Companion.REQUEST_CHECK_SETTINGS
import com.maged.currentweatherapp.databinding.ActivityMainBinding
import com.maged.currentweatherapp.util.*
import com.maged.currentweatherapp.util.AppUtils.getUnit
import com.maged.currentweatherapp.util.AppUtils.isNetworkAvailable
import com.maged.currentweatherapp.util.AppUtils.shouldAskLocationBGPermissions
import com.maged.currentweatherapp.util.AppUtils.shouldAskPermissions
import com.maged.currentweatherapp.viewmodel.WeatherViewModel
import com.maged.currentweatherapp.viewmodelfactory.WeatherViewModelFactory
import kotlinx.android.synthetic.main.content_weather.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), KodeinAware,
    ActivityCompat.OnRequestPermissionsResultCallback,
    PermissionUtils.PermissionResultCallback, GPSTracker.OnLocationDetected,
    GPSTracker.GpsLocationState {
    private var gpsTracker: GPSTracker? = null
    override val kodein by closestKodein()
    private lateinit var dataBind: ActivityMainBinding
    private val factory: WeatherViewModelFactory by instance()
    private val viewModel: WeatherViewModel by lazy {
        ViewModelProvider(this, factory).get(WeatherViewModel::class.java)
    }
    private var isPermissionsGranted: Boolean = false
    private lateinit var permissionUtils: PermissionUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBind = DataBindingUtil.setContentView(this, R.layout.activity_main)
        permissionUtils = PermissionUtils(this, this)
        observeAPICall()
        loadWeatherFeed("")
        dataBind.inputFindCityWeather.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loadWeatherFeed((view as EditText).text.toString())
            }
            false
        }
        dataBind.root.ll_time.setOnClickListener {
            loadWeatherFeed((dataBind.inputFindCityWeather as EditText).text.toString())
        }

    }

    @SuppressLint("SetTextI18n")
    private fun observeAPICall() {
        viewModel.weatherLiveData.observe(this, EventObserver { state ->
            when (state) {
                is State.Loading -> {
                }
                is State.Success -> {
                    if (isNetworkAvailable(this))
                        showSnackBar(dataBind.root, "Loading Live data")
                    else showSnackBar(dataBind.root, "Loading Local data")

                    state.data.let { weatherList ->
                        Log.i(
                            "NAME",
                            weatherList.description.toString()
                        )
                        dataBind.root.tv_time?.text =
                            getString(R.string.str_last_updated) + " " + AppUtils.parseDate(
                                weatherList.dateTime
                            )
                        dataBind.root.tv_main?.text =
                            weatherList.main
                        dataBind.root.tv_description?.text =
                            weatherList.description
                        dataBind.root.tv_temp?.text =
                            weatherList.temp.toString() + getUnit()
                        dataBind.root.tv_humidity.text =
                            weatherList.humidity.toString() + " per cent"
                        dataBind.root.tv_min.text = weatherList.tempMin.toString() + " min"
                        dataBind.root.tv_max?.text = weatherList.tempMax.toString() + " max"
                        dataBind.root.tv_speed?.text = weatherList.speed.toString() + " miles/hour"
                        dataBind.root.tv_name?.text = weatherList.name
                        dataBind.root.tv_country?.text = weatherList.countryName
                        dataBind.root.tv_sunrise?.text = unixTime(
                            weatherList.sunrise?.toLong()
                        )
                        dataBind.root.tv_sunset?.text = unixTime(
                            weatherList.sunset?.toLong()
                        )
                        val weatherIcon = weatherList.icon
                        AppUtils.setGlideImage(
                            dataBind.root.iv_main,
                            AppConstants.WEATHER_API_IMAGE_ENDPOINT + "${weatherIcon}@4x.png"
                        )
                        AppUtils.setGlideImage(
                            dataBind.clMain,
                            AppUtils.getImageUrlByCode(weatherIcon)
                        )
                    }
                }
                is State.Error -> {
                    showSnackBar(dataBind.root, state.message)
                }
            }
        })

        viewModel.weatherDetailListLiveData.observe(this, EventObserver { state ->
            when (state) {
                is State.Loading -> {
                }
                is State.Success -> {

                }
                is State.Error -> {
                    showSnackBar(dataBind.root, state.message)
                }
            }
        })
    }

    private fun loadWeatherFeed(query: String) {
        if (gpsTracker == null)
            gpsTracker = GPSTracker(this, this, this)
        var countryName = query
        if (countryName.isEmpty()) {
            countryName = gpsTracker!!.getCountryName(this)
            dataBind.inputFindCityWeather.setText(countryName)
        }
        if (!isPermissionsGranted) {
            if (shouldAskPermissions()) {
                val permissionList: ArrayList<String> = ArrayList()
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
                if (shouldAskLocationBGPermissions())
                    permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                permissionUtils.checkPermission(
                    permissionList,
                    "Need GPS to Get First Time Location for your country weather",
                    1
                )
            }
        } else
            weatherFeed(countryName)
    }


    private fun weatherFeed(query: String) {
        val password = BuildConfig.weather_api_key
        viewModel.fetchWeatherDetailFromDb(password, query)
        viewModel.fetchAllWeatherDetailsFromDb()
        gpsTracker?.removeListeners()
    }


    private fun unixTime(timex: Long?): String? {
        val date = Date(timex?.times(1000L)!!)
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionGranted(request_code: Int) {
        isPermissionsGranted = true
        gpsTracker?.displayLocationSettingsRequest(this)
    }

    override fun onPartialPermissionGranted(
        request_code: Int,
        granted_permissions: ArrayList<String>?
    ) {
    }

    override fun onPermissionDenied(request_code: Int) {
    }

    override fun onNeverAskAgain(request_code: Int) {
    }

    override fun gpsLocationState(resolvableApiException: ResolvableApiException?) {
        try {
            startIntentSenderForResult(
                resolvableApiException!!.resolution.intentSender,
                REQUEST_CHECK_GPS,
                null,
                0,
                0,
                0,
                null
            )
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onLocationDetected(latitude: Double?, longitude: Double?) {
        loadWeatherFeed("")
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            gpsTracker?.displayLocationSettingsRequest(this)
        }
        if (requestCode == REQUEST_CHECK_GPS) {
            if (resultCode == RESULT_OK) {
                gpsTracker?.displayLocationSettingsRequest(this)
            }
        }
    }

}