package com.maged.currentweatherapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.text.SimpleDateFormat
import java.util.*


object AppUtils {

    fun setGlideImage(view: ConstraintLayout, url: String?) {
        if (url == null || url.isEmpty()) {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    view.context,
                    android.R.color.holo_blue_light
                )
            )
        } else
            Glide.with(view)
                .load(url)
                .thumbnail(0.5f)
                .into(object : CustomTarget<Drawable?>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    override fun onResourceReady(
                        resource: Drawable,
                        @Nullable transition: Transition<in Drawable?>?
                    ) {
                        view.background = resource
                    }

                    override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                })
    }

    fun setGlideImage(image: ImageView, url: String) {
        Glide.with(image).load(url)
            .apply(RequestOptions().centerCrop())
            .into(image)
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentDateTime(dateFormat: String): String =
        SimpleDateFormat(dateFormat).format(Date())

    @SuppressLint("SimpleDateFormat")
    fun parseDate(date: String?): String {
        return when (date) {
            null -> SimpleDateFormat(AppConstants.DATE_FORMAT_1).format(
                Date()
            )
            else -> SimpleDateFormat(AppConstants.DATE_FORMAT_1).format(
                SimpleDateFormat(AppConstants.DATE_FORMAT_1).parse(date)
            )
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun isTimeExpired(dateTimeSavedWeather: String?): Boolean {
        dateTimeSavedWeather?.let {
            val currentDateTime = Date()
            val savedWeatherDateTime =
                SimpleDateFormat(AppConstants.DATE_FORMAT_1).parse(it)
            val diff: Long = currentDateTime.time - savedWeatherDateTime.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            if (minutes > 10)
                return true
        }
        return false
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return isOnline(connectivityManager)
        }
        return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isOnline(connectivityManager: ConnectivityManager): Boolean {

        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
        return false
    }

    fun shouldAskPermissions(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    fun shouldAskLocationBGPermissions(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun getUnit(): String {
        return "°C"
    }

    val images = hashMapOf(
        "01d" to
                "https://i.pinimg.com/236x/bb/3f/bb/bb3fbbf9a3808f4f718bb3ccb715e319.jpg",
        "01n" to
                "https://i.pinimg.com/236x/ec/e4/6b/ece46b2a4dbfed6b60fcb590cf7dec25.jpg",
        "02d" to
                "https://i.pinimg.com/236x/ec/8c/89/ec8c89d004db9e0c573dc0cf5ef70789.jpg",
        "02n" to
                "https://i.pinimg.com/564x/2f/a3/71/2fa37133ba670dd9012210af200c38e2.jpg",
        "03d" to
                "https://i.pinimg.com/236x/25/7d/df/257ddf9575c61ebca115d0946c22f56b.jpg",
        "03n" to
                "https://i.pinimg.com/750x/9b/9c/8e/9b9c8e1842fb15d561eef4ba92625e7f.jpg",
        "04d" to
                "https://i.pinimg.com/236x/73/44/a3/7344a3013c16c4053c4be0be96c0fa65.jpg",
        "04n" to
                "https://i.pinimg.com/236x/1d/5f/88/1d5f8851d7c1e6ac267bc7576346874b.jpg",
        "09d" to
                "https://i.pinimg.com/236x/cc/4a/2f/cc4a2f308e5d8e67f5e35d1766a426b3.jpg",
        "09n" to
                "https://i.pinimg.com/236x/a6/26/a8/a626a811bdb1c4b9cd8e75d97b8f09b7.jpg",
        "10d" to
                "https://i.pinimg.com/236x/47/f6/55/47f655e45e994095d686564ea70c74f4.jpg",
        "10n" to
                "https://i.pinimg.com/236x/b7/d0/5e/b7d05ea687d94cb113733990c5a159a8.jpg",
        "11d" to
                "https://i.pinimg.com/236x/5e/f8/e8/5ef8e8ed787d34b750b0d474032fdb40.jpg",
        "11n" to
                "https://i.pinimg.com/236x/1f/eb/fe/1febfe69ca7a59db3731b4b08c9b3aba.jpg",
        "13d" to
                "https://i.pinimg.com/236x/b1/92/e2/b192e20adee9a2f15d31944fc42d4ded.jpg",
        "13n" to
                "https://i.pinimg.com/236x/fe/f8/bd/fef8bd068d2518164c02c1f1960df490.jpg",
        "50d" to
                "https://i.pinimg.com/236x/ca/f3/37/caf337b72866074dc0f9849f93db63a7.jpg",
        "50n" to
                "https://i.pinimg.com/236x/95/7a/c8/957ac863cc538d9d865db0c5e3bfe170.jpg"
    )

    fun getImageUrlByCode(code: String?): String? {
        return images[code]
    }

}

