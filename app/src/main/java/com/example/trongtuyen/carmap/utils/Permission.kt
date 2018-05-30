package com.example.trongtuyen.carmap.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.util.Log

class Permission(activity: Activity, permission: String, listener: PermissionListener) {
    private var mActivity: Activity = activity
    private var mPermission: String = permission
    private var mListener: PermissionListener = listener

    companion object {
        const val TAG = "Permission"
    }

    interface PermissionListener {
        fun onPermissionGranted()
        fun onShouldProvideRationale()
        fun onRequestPermission()
        fun onPermissionDenied()
    }

    fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(mActivity, mPermission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)

//        // Should we show an explanation?
//        if (shouldProvideRationale) {
//            // Show an explanation to the user *asynchronously* -- don't block
//            // this thread waiting for the user's response! After the user
//            // sees the explanation, try again to request the permission.
//
//            mListener.onShouldProvideRationale()
//        } else {
            Log.i(TAG, "Requesting permission")
            // No explanation needed, we can request the permission.

            mListener.onRequestPermission()
//        }
    }

    fun execute() {
        if (checkPermissions()) {
            mListener.onPermissionGranted()
        } else {
            requestPermissions()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    fun onRequestPermissionsResult(grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted, yay! Do the
            mListener.onPermissionGranted()
        } else {

            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            mListener.onPermissionDenied()
        }
    }
}