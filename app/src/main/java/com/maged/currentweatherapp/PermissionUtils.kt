package com.maged.currentweatherapp

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

/**
 * Created by MagedSoham on 21/09/21.
 */
class PermissionUtils(context: Context, callback: PermissionResultCallback) {
    private var currentActivity: Activity = context as Activity
    private var permissionResultCallback: PermissionResultCallback = callback
    private var permissionList = ArrayList<String>()
    private var listPermissionsNeeded = ArrayList<String>()
    private var dialogContent = ""
    private var reqCode = 0

    /**
     * Check the API Level & Permission
     *
     * @param permissions
     * @param dialog_content
     * @param request_code
     */
    fun checkPermission(
        permissions: ArrayList<String>,
        dialog_content: String,
        request_code: Int
    ) {
        permissionList = permissions
        this.dialogContent = dialog_content
        reqCode = request_code
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions(permissions, request_code)) {
                permissionResultCallback.onPermissionGranted(request_code)
                Log.i("all permissions", "granted")
                Log.i("proceed", "to callback")
            }
        } else {
            permissionResultCallback.onPermissionGranted(request_code)
            Log.i("all permissions", "granted")
            Log.i("proceed", "to callback")
        }
    }

    /**
     * Check and request the Permissions
     *
     * @param permissions
     * @param request_code
     * @return
     */
    private fun checkAndRequestPermissions(
        permissions: ArrayList<String>,
        request_code: Int
    ): Boolean {
        if (permissions.size > 0) {
            listPermissionsNeeded = ArrayList()
            for (i in permissions.indices) {
                val hasPermission =
                    ContextCompat.checkSelfPermission(currentActivity, permissions[i])
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permissions[i])
                }
            }
            if (listPermissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    currentActivity,
                    listPermissionsNeeded.toTypedArray(),
                    request_code
                )
                return false
            }
        }
        return true
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty()) {
                val perms: MutableMap<String, Int> = HashMap()
                run {
                    var i = 0
                    while (i < permissions.size) {
                        perms[permissions[i]] = grantResults[i]
                        i++
                    }
                }
                val pendingPermissions = ArrayList<String>()
                var i = 0
                while (i < listPermissionsNeeded.size) {
                    if (perms[listPermissionsNeeded[i]] != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                currentActivity,
                                listPermissionsNeeded[i]
                            )
                        ) pendingPermissions.add(
                            listPermissionsNeeded[i]
                        ) else {
                            Log.i("Go to settings", "and enable permissions")
                            permissionResultCallback.onNeverAskAgain(reqCode)
                            Toast.makeText(
                                currentActivity,
                                "Go to settings and enable permissions",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }
                    }
                    i++
                }
                if (pendingPermissions.size > 0) {
                    showMessageOKCancel(
                        dialogContent
                    ) { _: DialogInterface?, which: Int ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> checkPermission(
                                permissionList,
                                dialogContent,
                                reqCode
                            )
                            DialogInterface.BUTTON_NEGATIVE -> {
                                Log.i("permisson", "not fully given")
                                if (permissionList.size == pendingPermissions.size) permissionResultCallback.onPermissionDenied(
                                    reqCode
                                ) else permissionResultCallback.onPartialPermissionGranted(
                                    reqCode,
                                    pendingPermissions
                                )
                            }
                        }
                    }
                } else {
                    Log.i("all", "permissions granted")
                    Log.i("proceed", "to next step")
                    permissionResultCallback.onPermissionGranted(reqCode)
                }
            }
        }
    }

    /**
     * Explain why the app needs permissions
     *
     * @param message
     * @param okListener
     */
    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(currentActivity)
            .setMessage(message)
            .setPositiveButton("Ok", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    interface PermissionResultCallback {
        fun onPermissionGranted(request_code: Int)
        fun onPartialPermissionGranted(request_code: Int, granted_permissions: ArrayList<String>?)
        fun onPermissionDenied(request_code: Int)
        fun onNeverAskAgain(request_code: Int)
    }
}