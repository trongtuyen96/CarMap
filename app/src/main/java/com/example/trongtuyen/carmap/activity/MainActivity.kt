package com.example.trongtuyen.carmap.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.*
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.R.id.*
import com.example.trongtuyen.carmap.activity.common.ReportMenuActivity
import com.example.trongtuyen.carmap.activity.common.SignInActivity
import com.example.trongtuyen.carmap.adapters.CustomInfoWindowAdapter
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Geometry
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.services.*
import com.example.trongtuyen.carmap.services.models.NearbyReportsResponse
import com.example.trongtuyen.carmap.services.models.UserProfileResponse
import com.example.trongtuyen.carmap.utils.Permission
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener, View.OnClickListener {

    // Static variables
    companion object {
        // PERMISSION_REQUEST_CODE
        private const val MY_LOCATION_PERMISSION_REQUEST_CODE = 1
        // Log
        private const val TAG = "MainActivity"
    }

    // Permission variables
    private lateinit var mLocationPermission: Permission

    // Google Map variables
    private lateinit var mMap: GoogleMap

    // Location variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    // PlaceAutoCompleteFragment
    private var placeAutoComplete: PlaceAutocompleteFragment? = null

    // Maerket options for set up marker
    private var markerOptions = MarkerOptions()

    // Popup windows
    private var mPopupWindowReport: PopupWindow? = null

    private var mPopupWindowUser: PopupWindow? = null

    // List of user of other cars
    private lateinit var listUser: List<User>

    // List of user of other cars
    private lateinit var listReport: List<Report>

    // Socket
    private lateinit var socket: Socket

    // List of user markers
    private var listUserMarker: MutableList<Marker> = ArrayList()

    // List of report markers
    private var listReportMarker: MutableList<Marker> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain placeAutoComplete fragment
        placeAutoComplete = fragmentManager.findFragmentById(R.id.place_autocomplete) as PlaceAutocompleteFragment
        placeAutoComplete!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Maps", "Place selected: " + place.name)
                addMarker(place)
            }

            override fun onError(status: Status) {
                Log.d("Maps", "An error occurred: $status")
            }
        })

        // Obtain MapFragment
        obtainMapFragment()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // Load user profile
        loadUserProfile()

        // Khởi tạo socket
        initSocket()

        // onClickListener cho các nút
        imvMyLoc.setOnClickListener(this)
        imvReport.setOnClickListener(this)
    }

    override fun onPause() {
        super.onPause()
//        Toast.makeText(this,"On Pause",Toast.LENGTH_SHORT).show()
        // stopLocationUpdates
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onStop() {
        super.onStop()
        //Toast.makeText(this,"On Stop",Toast.LENGTH_SHORT).show()
    }

    public override fun onResume() {
        super.onResume()
        Toast.makeText(this, "On Resume", Toast.LENGTH_SHORT).show()
        // resumeLocationUpdates ?
    }

    // Permission Requirement functions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.d(TAG, "requestCode: $requestCode")

        when (requestCode) {
            MY_LOCATION_PERMISSION_REQUEST_CODE -> mLocationPermission.onRequestPermissionsResult(grantResults)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationPermission() {
        mLocationPermission = Permission(this, android.Manifest.permission.ACCESS_FINE_LOCATION,
                object : Permission.PermissionListener {
                    override fun onPermissionGranted() {
                        if (mLocationPermission.checkPermissions()) {
                            mMap.isMyLocationEnabled = true
                            mMap.uiSettings.isMyLocationButtonEnabled = false
                        }
                        if (::fusedLocationClient.isInitialized) {
                            fusedLocationClient.lastLocation
                                    .addOnSuccessListener { location: Location? ->
                                        // Got last known location. In some rare situations this can be null.
                                        location ?: return@addOnSuccessListener
                                        lastLocation = location

                                        // Dùng khi chưa có grant permission - chạy lần đầu
                                        Toast.makeText(this@MainActivity, "Fused - init location permission", Toast.LENGTH_SHORT).show()
                                        if (::lastLocation.isInitialized) {
                                            val currentLatLng = LatLng(location.latitude, location.longitude)
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                                            val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
                                            val newGeo = Geometry("Point", listGeo)
                                            AppController.userProfile?.homeLocation = newGeo
                                        }
                                    }
                        }
                    }

                    override fun onShouldProvideRationale() {
                        Toast.makeText(this@MainActivity, "onShouldProvideRationale", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRequestPermission() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(
                                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_LOCATION_PERMISSION_REQUEST_CODE)
                        }
                    }

                    override fun onPermissionDenied() {
                        Toast.makeText(this@MainActivity, "onPermissionDenied", Toast.LENGTH_SHORT).show()
                    }

                })
    }

    // Google Map functions
    private fun obtainMapFragment() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imvMyLoc -> {
                onMyLocationButtonClicked()
            }

            R.id.imvReport -> {
                val intent = Intent(this, ReportMenuActivity::class.java)
                startActivity(intent)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Try catch parsing custom style json file to map
//        try {
//            // Customise the styling of the base map using a JSON object defined
//            // in a raw resource file: style_json.
//            var success = googleMap.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                            this, R.raw.style_json))
//
//            if (!success) {
//                Log.e("Resources", "Style parsing failed.")
//            }
//        } catch (e: Resources.NotFoundException) {
//            Log.e("Resources", "Can't find style. Error: ", e)
//        }

        //Set Custom InfoWindow Adapter
        val adapter = CustomInfoWindowAdapter(this)
        mMap.setInfoWindowAdapter(adapter)

        mMap.setOnMarkerClickListener(this)
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnInfoWindowCloseListener(this)

        initLocationPermission()

        mLocationPermission.execute()

        initLocation()

        // Set myLocationButton visible and clickable
    }

    // Location functions
    @SuppressLint("MissingPermission")
    private fun initLocation() {
        // Create location services client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (mLocationPermission.checkPermissions()) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations this can be null.
                        location ?: return@addOnSuccessListener
                        lastLocation = location

                        //
//                        moveMarker(markerOptions, LatLng(lastLocation.latitude, lastLocation.longitude))

                        // Khi đã có permission rồi, chạy trước locationCallback
                        Toast.makeText(this@MainActivity, "Fused success listener", Toast.LENGTH_SHORT).show()

                        if (::lastLocation.isInitialized) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                            val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
                            val newGeo = Geometry("Point", listGeo)
                            AppController.userProfile?.homeLocation = newGeo
                        }
                    }
        }

        // Define the location update callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    // Update UI with location data
                    // ...
                    lastLocation = location

                    // Nơi update location liên tục
                    Toast.makeText(this@MainActivity, "Update location callback", Toast.LENGTH_SHORT).show()

                    if (::lastLocation.isInitialized) {
                        val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
                        val newGeo = Geometry("Point", listGeo)
                        AppController.userProfile?.homeLocation = newGeo
                    }

                }
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

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
//                    e.startResolutionForResult(this@MainActivity,
//                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates() {
        @SuppressLint("MissingPermission")
        if (mLocationPermission.checkPermissions()) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */)
        }
    }

    @SuppressLint("MissingPermission")
    private fun onMyLocationButtonClicked() {
        if (::mMap.isInitialized) {
            if (::lastLocation.isInitialized) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 17f))
            }
        } else {
            Toast.makeText(this, "Vị trí hiện không khả dụng!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
//                onGetAllUser()
                onGetNearbyUsers()
            }
            R.id.nav_gallery -> {
                if (::lastLocation.isInitialized) {
                    val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)

                    val newGeo = Geometry("Point", listGeo)
                    Log.e("LOC", lastLocation.longitude.toString())
                    Log.e("LOC", lastLocation.latitude.toString())
                    val user = User("", "", "", "", "", "", "", newGeo)
                    onUpdateHomeLocation(user)
                }
            }
            R.id.nav_slideshow -> {
//                onGetAllReport()
                onGetNearbyReports()
            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_signout -> {
                onSignOut()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun addMarker(p: Place) {
        val markerOptions = MarkerOptions()
        markerOptions.position(p.latLng)
        markerOptions.title(p.name.toString() + "")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p.latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17f))
    }

    // ======================================================================
    // ======== ON NAVIGATION BUTTON EVENT ==================================
    // ======================================================================
    private fun loadUserProfile() {
        if (AppController.accessToken != null && AppController.accessToken.toString().length > 0) {
            val service = APIServiceGenerator.createService(UserService::class.java)
            val call = service.userProfile
            call.enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.isSuccessful) {
                        AppController.userProfile = response.body().user
                        updateInformation()
                    } else {
                        val apiError = ErrorUtils.parseError(response)
                        Toast.makeText(this@MainActivity, apiError.message(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {

                }
            })
        }
    }

    private fun updateInformation() {
        val user = AppController.userProfile
        tvName.text = user?.name
        tvEmail.text = user?.email
//        Helper.loadAvatarWithoutPlaceHolder(getActivity(), avatar, user.getAvatar(), net.diadiemmuasam.user.R.drawable.default_avatar)
    }

    private fun onSignOut() {
        AppController.signOut()
        Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        this.finish()
    }
    // ======================================================================


    // ======================================================================
    // ======== MARKER CLICK GROUP ==========================================
    // ======================================================================
    override fun onMarkerClick(p0: Marker): Boolean {
//        p0.showInfoWindow()
        onOpenReportMarker(p0)
        return false
    }

    private fun onOpenReportMarker(marker: Marker) {
        if (marker.title == "report") {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewReportPopup = inflater.inflate(R.layout.marker_report_layout, null)
            mPopupWindowReport = PopupWindow(viewReportPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupWindowReport!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)

            // Phải có con trỏ vào customViewPopup, nếu không sẽ null
            val tvType = viewReportPopup.findViewById<TextView>(R.id.tvType_marker_report)
            val tvDistance = viewReportPopup.findViewById<TextView>(R.id.tvDistance_marker_report)
            val tvLocation = viewReportPopup.findViewById<TextView>(R.id.tvLocation_marker_report)
            val tvDescription = viewReportPopup.findViewById<TextView>(R.id.tvDescription_marker_report)
            val imvType = viewReportPopup.findViewById<ImageView>(R.id.imvType_marker_report)
            val imvUpVote = viewReportPopup.findViewById<ImageView>(R.id.imvUpVote_marker_report)
            val imvDownVote = viewReportPopup.findViewById<ImageView>(R.id.imvDownVote_marker_report)

            val dataReport: Report = marker.tag as Report
            if (dataReport.subtype2 == "") {
                tvType.text = dataReport.subtype1
            } else {
                tvType.text = dataReport.subtype2
            }

            // Làm tròn số double
            val decimalFormat = DecimalFormat("#")
            decimalFormat.roundingMode = RoundingMode.CEILING

            tvDistance.text = "Cách " + decimalFormat.format(dataReport.distance) + " m"
            tvLocation.text = "Nguyen Kiem, Go Vap"
            tvDescription.text = dataReport.description.toString()
            when (dataReport.type) {
                "traffic" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_traffic)
                    when (dataReport.subtype1) {
                        "moderate" -> {
                            imvType.setImageResource(R.drawable.ic_report_traffic_moderate)
                        }
                        "heavy" -> {
                            imvType.setImageResource(R.drawable.ic_report_traffic_heavy)
                        }
                        "standstill" -> {
                            imvType.setImageResource(R.drawable.ic_report_traffic_standstill)
                        }
                    }
                }
                "crash" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_crash)
                    when (dataReport.subtype1) {
                        "minor" -> {
                            imvType.setImageResource(R.drawable.ic_accident_minor)
                        }
                        "major" -> {
                            imvType.setImageResource(R.drawable.ic_accident_major)
                        }
                        "other_side" -> {
                            imvType.setImageResource(R.drawable.ic_accident_other_side)
                        }
                    }
                }
                "hazard" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_hazard)
                    when (dataReport.subtype2) {
                        "object" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_object)
                        }
                        "construction" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_construction)
                        }
                        "broken_light" -> {
                            imvType.setImageResource(R.drawable.ic_report_broken_traffic_light)
                        }
                        "pothole" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_pothole)
                        }
                        "vehicle_stop" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_stopped)
                        }
                        "road_kill" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_roadkill)
                        }
                        "animal" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_animals)
                        }
                        "missing_sign" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_missingsign)
                        }
                        "fog" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_fog)
                        }
                        "hail" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_hail)
                        }
                        "flood" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_flood)
                        }
                        "ice" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_ice)
                        }
                    }
                }
                "help" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_assistance)
                    when (dataReport.subtype1) {
                        "no_gas" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_no_gas)
                        }
                        "flat_tire" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_flat_tire)
                        }
                        "no_battery" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_no_battery)
                        }
                        "medical_care" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_medical_care)
                        }
                    }
                }
            }
            imvUpVote.setOnClickListener {
                Toast.makeText(this, "Up Vote", Toast.LENGTH_SHORT).show()
                mPopupWindowReport!!.dismiss()
            }
            imvDownVote.setOnClickListener {
                Toast.makeText(this, "Down Vote", Toast.LENGTH_SHORT).show()
                mPopupWindowReport!!.dismiss()
            }
        }
        if (marker.title == "user") {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewUserPopup = inflater.inflate(R.layout.marker_user_layout, null)
            mPopupWindowUser = PopupWindow(viewUserPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupWindowUser!!.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)

            val imvAvatar = viewUserPopup.findViewById<ImageView>(R.id.imvAvatar_marker_user)
            val tvName = viewUserPopup.findViewById<TextView>(R.id.tvName_marker_user)
//            val tvDOB = viewReportPopup.findViewById<TextView>(R.id.tvDOB_marker_user)
            val tvEmail = viewUserPopup.findViewById<TextView>(R.id.tvEmail_marker_user)
            val btnHello = viewUserPopup.findViewById<Button>(R.id.btnHello_marker_user)

            val dataUser: User = marker.tag as User
            tvName.text = dataUser.name.toString()
//            tvDOB.text = dataUser.birthDate.toString()
            tvEmail.text = dataUser.email.toString()

            btnHello.setOnClickListener {
                attemptHello(AppController.userProfile?.name.toString(), dataUser.socketID.toString())
                mPopupWindowUser!!.dismiss()
            }
        }
    }

    // Sự kiện khi click vào info windows
    override fun onInfoWindowClick(p0: Marker) {
    }

    override fun onInfoWindowClose(p0: Marker?) {
        if (p0?.title == "report") {
            // Đóng popup windows
            mPopupWindowReport?.dismiss()
        }
        if (p0?.title == "user") {
            mPopupWindowUser?.dismiss()
        }
    }

    private fun moveMarker(marker: MarkerOptions, latLng: LatLng) {
        marker.position(latLng)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
    }

    private fun getUserFromMarker(marker: Marker): User {
        val listGeo: List<Double> = listOf(0.0, 0.0)
        val newGeo = Geometry("Point", listGeo)
        var user = User("", "", "", "", "", "", "", newGeo)
        for (i in 0 until listUser.size) {
            // Except current user
            if (listUser[i].email == marker.snippet) {
                user = listUser[i]
            }
        }
        return user
    }
    // ========================================================================


    // ========================================================================
    // ======== API CALL AND LISTENERS ========================================
    // ========================================================================
    private fun onGetAllUser() {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.allUserProfile
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    onAllUserProfileSuccess(response.body())
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Không có kết nối Internet", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun onAllUserProfileSuccess(response: List<User>) {
        listUser = response
        drawValidUsers()
    }

    private fun onGetNearbyUsers() {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.getNearbyUsers(lastLocation.latitude, lastLocation.longitude, 300f)
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Phạm vi 3 km", Toast.LENGTH_SHORT).show()
                    onAllUserProfileSuccess(response.body())
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Không có kết nối Internet", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun drawValidUsers() {
        if (listUser.size == 1) {
            Toast.makeText(this, "Không tìm thấy xe khác", Toast.LENGTH_SHORT).show()
        } else {
            if (listUserMarker.size > 0) {
                for (i in 0 until listUserMarker.size) {
                    listUserMarker[i].remove()
                }
                listUserMarker.clear()
            }
            for (i in 0 until listUser.size) {
                // Except current user
                if (listUser[i].email != AppController.userProfile!!.email)
                    addUserMarker(listUser[i])
            }
        }
    }

    private fun addUserMarker(user: User) {
        val markerOptions = MarkerOptions()
        // LatLag điền theo thứ tự latitude, longitude
        // Còn ở server Geo là theo thứ tự longitude, latitude
        // Random
        val random = Random()
        markerOptions.position(LatLng(user.homeLocation!!.coordinates!![1], user.homeLocation!!.coordinates!![0]))
        markerOptions.title("user")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(random.nextFloat() * 360))
        val marker = mMap.addMarker(markerOptions)
        listUserMarker.add(marker)
        marker.tag = user
    }

    private fun onUpdateHomeLocation(user: User) {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.updateHomeLocation(user)
        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Vị trí mới: " + "long:" + response.body().user?.homeLocation?.coordinates!![0] + "- lat: " + response.body().user?.homeLocation?.coordinates!![1], Toast.LENGTH_SHORT).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

    private fun onUpdateSocketID(user: User) {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.updateSocketID(user)
        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Socket ID hiện tại: " + response.body().user?.socketID, Toast.LENGTH_SHORT).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }
    // =====================================================================


    // ======================================================================
    // ======== SOCKET EVENT ================================================
    // ======================================================================
    private fun initSocket() {
        socket = SocketService().getSocket()
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
//        socket.on("chat message", onNewMessage)
        socket.on("hello message", onSayHello)
        socket.connect()
    }

    private val onConnect = Emitter.Listener {
        this.runOnUiThread({
            Toast.makeText(this.applicationContext,
                    "Đã kết nối socket", Toast.LENGTH_LONG).show()
            // Gán socket ID vào cho socketID của người dùng
            AppController.userProfile?.socketID = socket.id()
            onUpdateSocketID(AppController.userProfile!!)
        })
    }

    private val onDisconnect = Emitter.Listener {
        this.runOnUiThread({
            Toast.makeText(this.applicationContext,
                    "Ngắt kết nối socket", Toast.LENGTH_LONG).show()
        })
    }

    private val onConnectError = Emitter.Listener {
        this.runOnUiThread({
            Toast.makeText(this.applicationContext,
                    "Lỗi kết nối socket", Toast.LENGTH_LONG).show()
        })
    }

//    private val onNewMessage = Emitter.Listener { args ->
//        this.runOnUiThread(Runnable {
//            //            val data : JSONObject = args[0] as JSONObject
//            var message: String
//            try {
//                message = args[0] as String
//            } catch (e: JSONException) {
//                Log.e("LOG", e.message)
//                return@Runnable
//            }
//        })
//    }

    private val onSayHello = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {
            //            val data : JSONObject = args[0] as JSONObject
            val senderName: String
            val sendID: String
            val message: String
            try {
                senderName = args[0] as String
                sendID = args[1] as String
                message = args[2] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (message == "hello") {
                // Cần code lại layout
                val factory = LayoutInflater.from(this)
                val customDialogView = factory.inflate(R.layout.custom_dialog_layout, null)
                val customDialog = AlertDialog.Builder(this).create()
                customDialog.setView(customDialogView)
                customDialog.setOnShowListener(DialogInterface.OnShowListener {
                    customDialog.findViewById<TextView>(R.id.tv_custom_dialog).text = senderName + " đã chào bạn"
                    object : CountDownTimer(2000, 500) {

                        override fun onTick(millisUntilFinished: Long) {
                            customDialogView.findViewById<Button>(R.id.btn_custom_dialog).text = String.format(Locale.getDefault(), "%s (%d)",
                                    "Chào lại",
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
                        }

                        override fun onFinish() {
                            customDialog.dismiss()
                        }
                    }.start()
                })

                customDialogView.findViewById<Button>(R.id.btn_custom_dialog).setOnClickListener {
                    attemptHello(AppController.userProfile?.name.toString(), sendID)
                    customDialog.dismiss()
                }
                customDialog.show()
            }
        })
    }

    private fun attemptHello(senderName: String, receiveSocketID: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("say hello to someone", senderName, socket.id(), receiveSocketID, "hello")
    }
    // =====================================================================


    // ======================================================================
    // ======== REPORT ======================================================
    // ======================================================================
    private fun onGetAllReport() {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.allReport
        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                if (response.isSuccessful) {
                    onAllReportSuccess(response.body())
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Không có kết nối Internet", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun onGetNearbyReports() {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.getNearbyReports(lastLocation.latitude, lastLocation.longitude, 300f)
        call.enqueue(object : Callback<NearbyReportsResponse> {
            override fun onResponse(call: Call<NearbyReportsResponse>, response: Response<NearbyReportsResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Phạm vi 3 km", Toast.LENGTH_SHORT).show()
                    onNearbyReportsSuccess(response.body())
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    Toast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NearbyReportsResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Không có kết nối Internet", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun onAllReportSuccess(response: List<Report>) {
        listReport = response
//        // Gán vào listReport của AppController
//        AppController.listReport = response
        drawValidReports()
    }

    private fun onNearbyReportsSuccess(response: NearbyReportsResponse) {
        listReport = response.reports!!

//        // Gán vào listReport của AppController
//        AppController.listReport = response.reports!!

        for (i in 0 until response.reports!!.size) {
            listReport[i].distance = response.distances!![i]
        }

        drawValidReports()

    }

    private fun drawValidReports() {
        if (listReportMarker.size > 0) {
            for (i in 0 until listReportMarker.size) {
                listReportMarker[i].remove()
            }
            listReportMarker.clear()
        }
        for (i in 0 until listReport.size) {
            addReportMarker(listReport[i])
        }
    }

    private fun addReportMarker(report: Report) {
        val markerOptions = MarkerOptions()
        // LatLag điền theo thứ tự latitude, longitude
        // Còn ở server Geo là theo thứ tự longitude, latitude
//        Log.e("REPORT", report.geometry!!.coordinates!![1].toString() + " " +  report.geometry!!.coordinates!![0].toString())
        markerOptions.position(LatLng(report.geometry!!.coordinates!![1], report.geometry!!.coordinates!![0]))
        markerOptions.title("report")
//        markerOptions.snippet(report._id.toString())
        when (report.type.toString()) {
            "traffic" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_bar_report_trafficjam))
            }
            "crash" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_bar_report_accident))
            }
            "hazard" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_bar_report_hazard))
            }
            "assistance" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_bar_report_assistance))
            }
        }
        val marker = mMap.addMarker(markerOptions)
        listReportMarker.add(marker)
        marker.tag = report
    }
    // ======================================================================
}
