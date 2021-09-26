package com.maged.currentweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.io.IOException
import java.util.*

/**
 * Created by MagedSoham on 21/09/2021
 */
class GPSTracker(
    mContext: Context,
    val onLocationDetected: OnLocationDetected,
    private val gpsLocationState: GpsLocationState
) : Service() {
    private var locationCallback: LocationCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var location: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var geocoderMaxResults = 1

    /**
     * Update GPSTracker latitude and longitude
     */
    private fun updateGPSCoordinates() {
        if (location != null) {
            latitude = location!!.latitude
            longitude = location!!.longitude
        }
    }

    /**
     * Get list of address by latitude and longitude
     *
     * @return null or List<Address>
    </Address> */
    private fun getGeocoderAddress(context: Context?): List<Address>? {
        if (location != null) {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            try {
                return geocoder.getFromLocation(latitude, longitude, geocoderMaxResults)
                /**
                 * Geocoder.getFromLocation - Returns an array of Addresses
                 * that are known to describe the area immediately surrounding the given latitude and longitude.
                 */
            } catch (e: IOException) {
                //e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e)
            }
        }
        return null
    }


    /**
     * Try to get CountryName
     *
     * @return null or postalCode
     */
    fun getCountryName(context: Context?): String {
        val addresses = getGeocoderAddress(context)
        return if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            address.countryName
        } else {
            ""
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        const val REQUEST_CHECK_SETTINGS = 17788
        const val REQUEST_CHECK_GPS = 17789

        // Get Class Name
        private val TAG = GPSTracker::class.java.name
    }

    init {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext)
        getCurrentLocation(mContext)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(context: Context) {
        if (checkIfDeviceHaveGPSProvider(context) || !checkIfUserEnableLocation(context)) {
            updateGPSCoordinates()
        } else {
            displayLocationSettingsRequest(context)
        }
    }

    private fun checkIfDeviceHaveGPSProvider(context: Context?): Boolean {
        val locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.allProviders.contains(LocationManager.GPS_PROVIDER)
    }

    private fun checkIfUserEnableLocation(context: Context?): Boolean {
        val lm = context?.getSystemService(LOCATION_SERVICE) as LocationManager
        val gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps_enabled && network_enabled
    }

    fun removeListeners() {
        if (locationCallback != null) fusedLocationProviderClient?.removeLocationUpdates(
            locationCallback
        )
    }

    interface GpsLocationState {
        fun gpsLocationState(resolvableApiException: ResolvableApiException?)
    }

    interface OnLocationDetected {
        fun onLocationDetected(latitude: Double?, longitude: Double?)
    }

    fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task: Task<LocationSettingsResponse?> ->
            try {
                task.getResult(
                    ApiException::class.java
                )
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (locationTemp in locationResult.locations) {
                            if (locationTemp != null) {
                                location = locationTemp
                                latitude = locationTemp.latitude
                                longitude = locationTemp.longitude
                                onLocationDetected.onLocationDetected(latitude, longitude)
                            }
                        }
                    }
                }
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) return@addOnCompleteListener
                fusedLocationProviderClient!!.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable = exception as ResolvableApiException
                            gpsLocationState.gpsLocationState(resolvable)
                        } catch (e: java.lang.Exception) {
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

}