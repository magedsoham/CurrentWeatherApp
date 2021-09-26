package com.maged.currentweatherapp.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar


// used for show a toast message in the UI Thread
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showSnackBar(view: View, message: String) {
    val snackbar = Snackbar
        .make(view, message, Snackbar.LENGTH_LONG)
    val group = snackbar.view as ViewGroup
    group.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_dark))
    snackbar.show()
}


fun Activity.color(resId: Int): Int {
    return ContextCompat.getColor(this, resId)
}


