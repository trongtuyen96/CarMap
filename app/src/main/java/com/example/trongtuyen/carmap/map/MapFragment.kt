package com.example.trongtuyen.carmap.map

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.trongtuyen.carmap.BuildConfig
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.utils.Permission
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var mLocationPermission: Permission

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private lateinit var locationRequest: LocationRequest

    private lateinit var lastLocation: Location

    companion object {
        fun newInstance():MapFragment{
            return MapFragment()
        }
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val TAG = "MapFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Call inflate first to get activity, view
        val view = inflater.inflate(R.layout.app_bar_main, container, false)

        initMap()

        return view
    }

    private  fun initMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        initComponents()
    }

    private fun initComponents(){
        initLocationPermission()

        mLocationPermission.execute()

        initLocation()

//        fab.isClickable = true
//        fab.visibility = View.VISIBLE
//
//        fab.setOnClickListener {onMyLocationButtonClicked()}
    }

    @SuppressLint("MissingPermission")
    private fun initLocationPermission(){
        mLocationPermission = Permission(this.activity!!,android.Manifest.permission.ACCESS_FINE_LOCATION,
                object : Permission.PermissionListener {

                    override fun onPermissionGranted() {
//                if (ActivityCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED
//                        && ActivityCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED){
//                    Toast.makeText(activity, "Quyền truy cập vị trí chưa được cho phép!",
//                            Toast.LENGTH_SHORT).show()
//                    return
//                }

                        if (mLocationPermission.checkPermissions()){
                            mMap.isMyLocationEnabled = true
                            mMap.uiSettings.isMyLocationButtonEnabled = false
                        }
                        if (::fusedLocationClient.isInitialized){
                            fusedLocationClient.lastLocation
                                    .addOnSuccessListener { location : Location? ->
                                        // Got last known location. In some rare situations this can be null.
                                        location?: return@addOnSuccessListener
                                        lastLocation=location
                                    }
                        }
                    }

                    override fun onShouldProvideRationale() {
                        Snackbar.make(view!!.findViewById(android.R.id.content),"Permission is denied",
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", {
                                    // Request permission
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                                LOCATION_PERMISSION_REQUEST_CODE)
                                    }
                                }).show()
//                Toast.makeText(activity,"Snackbar crash",Toast.LENGTH_SHORT).show()
                    }

                    override fun onRequestPermission() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(
                                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    LOCATION_PERMISSION_REQUEST_CODE)
                        }
                    }

                    override fun onPermissionDenied() {
                        Snackbar.make(
                                view!!.findViewById(R.id.map),
                                "Permission is required",
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction("SETTINGS", {openAppSettings()})
                                .show()
//                Toast.makeText(activity,"Snackbar crash",Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.data = uri
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun initLocation(){
        // Create location services client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)

        if (mLocationPermission.checkPermissions()){
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                        location?:return@addOnSuccessListener
                        lastLocation=location
                    }
        }

        // Define the location update callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                super.onLocationResult(locationResult)
//                for (location in locationResult.locations){
//                    // Update UI with location data
//                    // ...
//                    lastLocation = location
//                }
            }
        }

        // Set up a location request
        createLocationRequest()
    }

    @SuppressLint("RestrictedApi")
    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

//        val builder = LocationSettingsRequest.Builder()
//                .addLocationRequest(locationRequest)
//
//        // 4
//        val client = LocationServices.getSettingsClient(activity!!)
//        val task = client.checkLocationSettings(builder.build())
//
//        // 5
//        task.addOnSuccessListener {
//            startLocationUpdates()
//        }
//        task.addOnFailureListener { e ->
//            // 6
//            if (e is ResolvableApiException) {
//                // Location settings are not satisfied, but this can be fixed
//                // by showing the user a dialog.
//                try {
//                    // Show the dialog by calling startResolutionForResult(),
//                    // and check the result in onActivityResult().
//                    e.startResolutionForResult(this@MainActivity,
//                            REQUEST_CHECK_SETTINGS)
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    // Ignore the error.
//                }
//            }
//        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (mLocationPermission.checkPermissions()){
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()

        // stopLocationUpdate
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun onMyLocationButtonClicked(){
        mLocationPermission.execute()

        startLocationUpdates()

        if (::lastLocation.isInitialized){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 17f))
        }
    }
}