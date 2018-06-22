package com.example.trongtuyen.carmap.activity

import `in`.championswimmer.sfg.lib.SimpleFingerGestures
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Configuration
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.*
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.R.id.*
import com.example.trongtuyen.carmap.activity.common.*
import com.example.trongtuyen.carmap.adapters.CustomInfoWindowAdapter
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Geometry
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.models.direction.DirectionFinder
import com.example.trongtuyen.carmap.models.direction.Route
import com.example.trongtuyen.carmap.services.*
import com.example.trongtuyen.carmap.services.models.NearbyReportsResponse
import com.example.trongtuyen.carmap.services.models.ReportResponse
import com.example.trongtuyen.carmap.services.models.SampleResponse
import com.example.trongtuyen.carmap.services.models.UserProfileResponse
import com.example.trongtuyen.carmap.utils.FileUtils
import com.example.trongtuyen.carmap.utils.Permission
import com.example.trongtuyen.carmap.utils.SharePrefs.Companion.mContext
import com.github.angads25.toggle.LabeledSwitch
import com.github.angads25.toggle.interfaces.OnToggledListener
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
import com.sdsmdg.tastytoast.TastyToast
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.UnsupportedEncodingException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener, View.OnClickListener, DirectionFinder.DirectionListener, GoogleMap.OnPolylineClickListener {

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

    private lateinit var mActionBarDrawerToggle: ActionBarDrawerToggle

    // Maerket options for set up marker
    private var markerOptions = MarkerOptions()

    // Popup windows
    private var mPopupWindowReport: PopupWindow? = null

    private var mPopupWindowUser: PopupWindow? = null

    private var mPopupWindowHello: PopupWindow? = null

    private var mPopupWindowFilter: PopupWindow? = null

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

    // Handler của thread
    private lateinit var handler: Handler

    // Runnable của auto
    private lateinit var runnable: Runnable

    // Marker id để check marker nào đang được ấn
    private var curMarkerReport: Marker? = null

    // Marker id để check marker nào đang được ấn
    private var curMarkerUser: Marker? = null

    // Gesture Detector
    private lateinit var mDetector: GestureDetector

    // Place Info
    private var mPopupWindowPlaceInfo: PopupWindow? = null

    private var isPlaceInfoWindowUp = false

    private var currentSelectedPlace: Place? = null

    // Của Direction
    private lateinit var polylinePaths: MutableList<Polyline>
    private var originMarkers: MutableList<Marker>? = ArrayList()
    private var destinationMarkers: MutableList<Marker>? = ArrayList()


    // ==================================================================================================================================== //
    // ======== VỀ DIRECTION ============================================================================================================== //
    // ==================================================================================================================================== //
    private fun showPlaceInfoPopup(place: Place) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewPlacePopup = inflater.inflate(R.layout.place_info_layout, null)
        mPopupWindowPlaceInfo = PopupWindow(viewPlacePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mPopupWindowPlaceInfo!!.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)
        isPlaceInfoWindowUp = true

        val tvPlaceName = viewPlacePopup.findViewById<TextView>(R.id.tvPlaceName_place_info)
        val tvPlaceAddress = viewPlacePopup.findViewById<TextView>(R.id.tvPlaceAddress_place_info)
        val btnStartDirection = viewPlacePopup.findViewById<LinearLayout>(R.id.btnStartDirection_place_info)
        val btnSelectedPlace = viewPlacePopup.findViewById<LinearLayout>(R.id.btnSelectedPlace_place_info)

        tvPlaceName.text = place.name
        tvPlaceAddress.text = place.address

        btnStartDirection.setOnClickListener {
            onBtnStartDirectionClick(place)
        }

        btnSelectedPlace.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 17f))
        }
//        imvReport.visibility = View.GONE
    }

    private fun onBtnStartDirectionClick(place: Place) {
        val origin = lastLocation.latitude.toString() + "," + lastLocation.longitude.toString()

        val destination = place.name.toString()

        try {
            DirectionFinder(this, origin, destination).execute()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    private fun removeCurrentDirectionPolyline() {
        if (originMarkers != null) {
            for (marker in originMarkers!!) {
                marker.remove()
            }
        }

        if (destinationMarkers != null) {
            for (marker in destinationMarkers!!) {
                marker.remove()
            }
        }

        if (::polylinePaths.isInitialized) {
            for (polyline in polylinePaths) {
                polyline.remove()
            }
            polylinePaths.clear()
        }
    }

    override fun onDirectionFinderStart() {
        removeCurrentDirectionPolyline()
    }

    private lateinit var currentPolyline: Polyline

    override fun onPolylineClick(p0: Polyline) {
        if (p0 == currentPolyline)
            return
        p0.color = Color.BLUE
        currentPolyline.color = Color.GRAY
        p0.zIndex = 1F
        currentPolyline.zIndex = 0F
        currentPolyline = p0
        val currentRoute = currentPolyline.tag as Route
        Toast.makeText(this, currentRoute.duration!!.text + " | " + currentRoute.distance!!.text, Toast.LENGTH_SHORT).show()
    }

    override fun onDirectionFinderSuccess(routes: List<Route>) {
        polylinePaths = ArrayList()
        originMarkers = ArrayList<Marker>()
        destinationMarkers = ArrayList<Marker>()
        var firstRoute = true

        for (route in routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16f))

            val polylineOptions = PolylineOptions().geodesic(true).width(10f).color(Color.GRAY)

            val polyline = drawPolyline(route, polylineOptions)

            if (firstRoute) {
                firstRoute = false
                currentPolyline = polyline
                currentPolyline.zIndex = 1F
                currentPolyline.color = Color.BLUE
            }
        }
    }

    private fun drawPolyline(route: Route, options: PolylineOptions): Polyline {
        for (i in 0 until route.points!!.size)
            options.add(route.points!![i])

        val polyline = mMap.addPolyline(options)
        polyline.isClickable = true
        polyline.tag = route
        polylinePaths.add(polyline)
        return polyline
    }


    // ========================================================================================================================================= //
    // ======== VỀ MAIN ======================================================================================================================== //
    // ========================================================================================================================================= //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // Obtain placeAutoComplete fragment
        placeAutoComplete = fragmentManager.findFragmentById(R.id.place_autocomplete) as PlaceAutocompleteFragment
        placeAutoComplete!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Maps", "Place selected: " + place.name)
                dismissPopupWindowPlaceInfo()
                removeCurrentSelectedPlace()
                currentSelectedPlace = place

                addMarker(place)

                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17f))
                showPlaceInfoPopup(place)
            }

            override fun onError(status: Status) {
                Log.d("Maps", "An error occurred: $status")
            }
        })

        obtainMapFragment()

        initActionBarDrawerToggle()

//        nav_view.setNavigationItemSelectedListener(this)

//        // Set up Hamburger button toggle Navigation Drawer
//        setupHamburgerButton()

        // Load user profile
        loadUserProfile()

        // Khởi tạo socket
        initSocket()

        // onClickListener cho các nút
        imvMyLoc.setOnClickListener(this)
        imvReport.setOnClickListener(this)
//        imvHamburger.setOnClickListener(this)

        imvFilter.setOnClickListener(this)


        // Khởi tạo các nút trên menu drawer
        initMenuItemDrawer()

        mContext = this.applicationContext
    }

    private fun initMenuItemDrawer() {

        layoutHomeMenu.setOnClickListener(this)

        layoutWorkMenu.setOnClickListener(this)

        layoutHistoryMenu.setOnClickListener(this)

        layoutSettingMenu.setOnClickListener(this)

        layoutQuickSettingSound.setOnClickListener(this)
        // Set lần đầu cho setting âm thanh
        when (AppController.soundMode) {
            1 -> {
                AppController.soundMode = 2
                imQuickSettingSound.setImageResource(R.drawable.ic_sound_alerts)
                tvQuickSettingSound.text = "CHỈ CÁC BÁO HIỆU"
            }
            2 -> {
                AppController.soundMode = 3
                imQuickSettingSound.setImageResource(R.drawable.ic_sound_mute)
                tvQuickSettingSound.text = "TẮT"
            }
            3 -> {
                AppController.soundMode = 1
                imQuickSettingSound.setImageResource(R.drawable.ic_sound_on)
                tvQuickSettingSound.text = "MỞ"
            }
        }

        layoutSignOut.setOnClickListener(this)

        layoutEditHome.setOnClickListener(this)

        layoutEditWork.setOnClickListener(this)
    }

    private var isTouchSoundsEnabled: Boolean = false
    private var isTouchVibrateEnabled: Boolean = false
    private fun initSoundVibrate() {
        if (Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) == 0) {
            Settings.System.putInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1)
            isTouchSoundsEnabled = true
        }
        if (Settings.System.getInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) {
            Settings.System.putInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1)
            isTouchVibrateEnabled = true
        }
    }

    private fun destroySoundVibrate() {
        if (isTouchSoundsEnabled == true) {
            Settings.System.putInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 0)
        }
        if (isTouchVibrateEnabled == true) {
            Settings.System.putInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0)
        }
    }

    override fun onPause() {
        super.onPause()
//        Toast.makeText(this,"On Pause",Toast.LENGTH_SHORT).show()
        // stopLocationUpdates
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    public override fun onStop() {
        super.onStop()
        // Toast.makeText(this, "On Stop", Toast.LENGTH_SHORT).show()
        // Xoá runnable thread
        handler.removeCallbacks(runnable)

//        // Destroy socket
//        destroySocket()

        // Destroy sound and vibrate
        destroySoundVibrate()
    }

    public override fun onResume() {
        super.onResume()
//         Toast.makeText(this, "On Resume", Toast.LENGTH_SHORT).show()
        // resumeLocationUpdates ?

        // Khởi tạo socket
//        initSocket()

        // Bắt sự kiện socket report other
        if (AppController.base64ImageReportOther != "" || AppController.licensePlate != "") {
//            Log.e("SOCKET", AppController.base64ImageReportOther)
//            Toast.makeText(this, "Vô chỗ gửi rồi", Toast.LENGTH_SHORT).show()
            for (i in 0 until listUser.size) {
                if (listUser[i].email != AppController.userProfile!!.email) {
                    attemptReportOther(AppController.userProfile!!.email!!, listUser[i].socketID.toString(), AppController.typeReportOther, AppController.base64ImageReportOther, AppController.licensePlate)
//                    Log.e("SOCKET", AppController.userProfile!!.email!!)
//                    Log.e("SOCKET", listUser[i].socketID.toString())
//                    Log.e("SOCKET", AppController.typeReportOther)
//                    Log.e("SOCKET", AppController.base64ImageReportOther)
//                    Log.e("SOCKET", AppController.licensePlate)
                }
            }
            AppController.base64ImageReportOther = ""
            AppController.licensePlate = ""
        }

        // Tự động thực hiện
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                // Sau đó lặp lại mỗi 15s
                if (::lastLocation.isInitialized) {
                    // Toast.makeText(this@MainActivity, "Cập nhật All", Toast.LENGTH_SHORT).show()
                    // Cập nhật địa điểm hiện tại
                    val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
                    val newGeo = Geometry("Point", listGeo)
                    val user = User("", "", "", "", "", "", "", newGeo, 0.0, 0.0, 0.0, 0.0)
                    onUpdateCurrentLocation(user)

                    // Cập nhật người dùng xung quanh
                    onGetNearbyUsers()

                    // Cập nhật biển báo xung quanh
                    onGetNearbyReports()

//                    // Gán user hiện tại từ listUser vào AppController
//                    var i = 0
//                    if (::listUser.isInitialized) {
//                        if (listUser.isNotEmpty()) {
//                            Toast.makeText(this@MainActivity, i.toString(),Toast.LENGTH_SHORT).show()
//                            while (AppController.userProfile?._id != listUser[i]._id) {
//                                i += 1
//                            }
//                            AppController.userProfile = listUser[i]
//                        }
//                    }
                }
                handler.postDelayed(this, 15000)
            }
        }

        // Lần đầu chạy sau 5s
        handler.postDelayed(runnable, 5000)  //the time is in miliseconds

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
            // Khởi tạo sound và vibrate
            initSoundVibrate()
        } else {
            // Migrate to Setting write permission screen
            val intent: Intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.setData(Uri.parse("package:" + this.getPackageName()))
            startActivity(intent);
            TastyToast.makeText(this, "Cho phép chỉnh sửa cài đặt hệ thống", TastyToast.LENGTH_LONG, TastyToast.DEFAULT)
        }
    }

    private fun initActionBarDrawerToggle() {
        mActionBarDrawerToggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(mActionBarDrawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mActionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mActionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onClick(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        when (v.id) {
            R.id.imvMyLoc -> {
                onMyLocationButtonClicked()
            }

            R.id.imvReport -> {
                val intent = Intent(this, ReportMenuActivity::class.java)
                startActivity(intent)
            }
            R.id.imvFilter -> {
                if (mPopupWindowFilter != null) {
                    if (mPopupWindowFilter!!.isShowing) {
                        mPopupWindowFilter!!.dismiss()
                    } else {
                        onFilterButtonClicked()
                    }
                } else {
                    onFilterButtonClicked()
                }
            }
            R.id.layoutHomeMenu -> {

            }
            R.id.layoutWorkMenu -> {

            }
            R.id.layoutHistoryMenu -> {

            }
            R.id.layoutSettingMenu -> {

            }
            R.id.layoutQuickSettingSound -> {
                when (AppController.soundMode) {
                    1 -> {
                        AppController.soundMode = 2
                        imQuickSettingSound.setImageResource(R.drawable.ic_sound_alerts)
                        tvQuickSettingSound.text = "CHỈ CÁC BÁO HIỆU"
                    }
                    2 -> {
                        AppController.soundMode = 3
                        imQuickSettingSound.setImageResource(R.drawable.ic_sound_mute)
                        tvQuickSettingSound.text = "TẮT"
                    }
                    3 -> {
                        AppController.soundMode = 1
                        imQuickSettingSound.setImageResource(R.drawable.ic_sound_on)
                        tvQuickSettingSound.text = "MỞ"
                    }
                }

            }
            R.id.layoutSignOut -> {
                onSignOut()
            }
            R.id.layoutEditHome -> {
                val intent = Intent(this, HomeSettingActivity::class.java)
                intent.putExtra("home_location", tvAddressHome_menu.text.toString())
                startActivityForResult(intent, 1)
            }
            R.id.layoutEditWork -> {
                val intent = Intent(this, WorkSettingActivity::class.java)
                intent.putExtra("work_location", tvAddressWork_menu.text.toString())
                startActivityForResult(intent, 2)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    tvAddressHome_menu.text = data!!.getStringExtra("home_location_new")
                    val listGeo: List<Double> = listOf(0.0, 0.0)
                    val newGeo = Geometry("Point", listGeo)
                    val user = User("", "", "", "", "", "", "", newGeo, AppController.userProfile!!.latHomeLocation!!, AppController.userProfile!!.longHomeLocation!!, 0.0, 0.0)
                    onUpdateHomeLocation(user)
                }
            }
            2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    tvAddressWork_menu.text = data!!.getStringExtra("work_location_new")
                    val listGeo: List<Double> = listOf(0.0, 0.0)
                    val newGeo = Geometry("Point", listGeo)
                    val user = User("", "", "", "", "", "", "", newGeo, 0.0, 0.0, AppController.userProfile!!.latWorkLocation!!, AppController.userProfile!!.longWorkLocation!!)
                    onUpdateWorkLocation(user)
                }
            }
        }

    }


    // ================================================================================================================================================== //
    // ======== VỀ PERMISSION LOCATION VÀ MAP =========================================================================================================== //
    // ================================================================================================================================================== //

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
                                        // Toast.makeText(this@MainActivity, "Fused - init location permission", Toast.LENGTH_SHORT).show()
                                        if (::lastLocation.isInitialized) {
                                            val currentLatLng = LatLng(location.latitude, location.longitude)
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                                            val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
                                            val newGeo = Geometry("Point", listGeo)
                                            AppController.userProfile?.currentLocation = newGeo
                                        }
                                    }
                        }
                    }

                    override fun onShouldProvideRationale() {
                        // Toast.makeText(this@MainActivity, "onShouldProvideRationale", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRequestPermission() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(
                                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_LOCATION_PERMISSION_REQUEST_CODE)
                        }
                    }

                    override fun onPermissionDenied() {
                        // Toast.makeText(this@MainActivity, "onPermissionDenied", Toast.LENGTH_SHORT).show()
                    }

                })
    }

    // Google Map functions
    private fun obtainMapFragment() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap.setOnPolylineClickListener(this)

        initLocationPermission()

        mLocationPermission.execute()

        initLocation()

        // Set myLocationButton visible
        imvMyLoc.visibility = View.VISIBLE
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
                        // Toast.makeText(this@MainActivity, "Fused success listener", Toast.LENGTH_SHORT).show()

                        if (::lastLocation.isInitialized) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                            val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
                            val newGeo = Geometry("Point", listGeo)
                            AppController.userProfile?.currentLocation = newGeo
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
                    // Toast.makeText(this@MainActivity, "Update location callback", Toast.LENGTH_SHORT).show()

                    if (::lastLocation.isInitialized) {
                        val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
                        val newGeo = Geometry("Point", listGeo)
                        AppController.userProfile?.currentLocation = newGeo
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


    // ================================================================================================================================================== //
    // ======== VỀ CÁC NÚT TRÊN APP BAR MAIN ============================================================================================================ //
    // ================================================================================================================================================== //

    @SuppressLint("MissingPermission")
    private fun onMyLocationButtonClicked() {
        if (::mMap.isInitialized) {
            if (::lastLocation.isInitialized) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 17f))
            }
        } else {
            TastyToast.makeText(this, "Vị trí hiện không khả dụng!", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
        }
    }

    private fun onFilterButtonClicked() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewFilterPopup = inflater.inflate(R.layout.filter_dialog_layout, null)
        mPopupWindowFilter = PopupWindow(viewFilterPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mPopupWindowFilter!!.showAtLocation(this.currentFocus, Gravity.NO_GRAVITY, (imvFilter.x.toInt() / 2) - (imvFilter.x.toInt() / 6), imvFilter.y.toInt())

        val btnClose = viewFilterPopup.findViewById<ImageView>(R.id.imCLose_filter_dialog)
        val switchCar = viewFilterPopup.findViewById<LabeledSwitch>(R.id.switchFilterCar_filter_dialog)
        val switchReport = viewFilterPopup.findViewById<LabeledSwitch>(R.id.switchFilterReport_filter_dialog)

        if (AppController.settingFilterCar == "true") {
            switchCar.isOn = true
        } else {
            switchCar.isOn = false
        }

        if (AppController.settingFilterReport == "true") {
            switchReport.isOn = true
        } else {
            switchReport.isOn = false
        }


        btnClose.setOnClickListener {
            mPopupWindowFilter!!.dismiss()
            if (AppController.settingFilterCar == "true") {
                if (::listUser.isInitialized) {
                    if (listUser.isNotEmpty()) {
                        drawValidUsers()
                    }
                }
            } else {
                for (i in 0 until listUserMarker.size) {
                    listUserMarker[i].remove()
                }
            }
            if (AppController.settingFilterReport == "true") {
                if (::listReport.isInitialized) {
                    if (listReport.isNotEmpty()) {
                        drawValidReports()
                    }
                }
            } else {
                for (i in 0 until listReportMarker.size) {
                    listReportMarker[i].remove()
                }
            }
        }

        switchCar.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
//                    Toast.makeText(this@MainActivity, "Car on", Toast.LENGTH_SHORT).show()
                    AppController.settingFilterCar = "true"
                } else {
//                    Toast.makeText(this@MainActivity, "Car off", Toast.LENGTH_SHORT).show()
                    AppController.settingFilterCar = "false"
                }
            }
        })

        switchReport.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(labeledSwitch: LabeledSwitch?, isOn: Boolean) {
                if (isOn) {
//                    Toast.makeText(this@MainActivity, "Report on", Toast.LENGTH_SHORT).show()
                    AppController.settingFilterReport = "true"
                } else {
//                    Toast.makeText(this@MainActivity, "Report off", Toast.LENGTH_SHORT).show()
                    AppController.settingFilterReport = "false"
                }
            }
        })
    }

    override fun onBackPressed() {
//        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
//            drawer_layout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> {
                drawer_layout.closeDrawer(GravityCompat.START)
                return
            }
            ::polylinePaths.isInitialized && polylinePaths.isNotEmpty() -> {
                removeCurrentDirectionPolyline()
                return
            }
            isPlaceInfoWindowUp -> {
                dismissPopupWindowPlaceInfo()
                return
            }
            currentSelectedPlace != null -> {
                removeCurrentSelectedPlace()
                return
            }
            else -> super.onBackPressed()
        }
    }

    private fun dismissPopupWindowPlaceInfo() {
        mPopupWindowPlaceInfo?.dismiss()
        isPlaceInfoWindowUp = false
        imvReport.visibility = View.VISIBLE
    }

    private fun removeCurrentSelectedPlace() {
        currentSelectedPlaceMarker?.remove()
        placeAutoComplete?.setText(null)
        currentSelectedPlace = null
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        // Handle navigation view item clicks here.
//        when (item.itemId) {
//            R.id.nav_camera -> {
////                onGetAllUser()
//                onGetNearbyUsers()
//            }
//            R.id.nav_gallery -> {
//                if (::lastLocation.isInitialized) {
//                    val listGeo: List<Double> = listOf(lastLocation.longitude, lastLocation.latitude)
//
//                    val newGeo = Geometry("Point", listGeo)
//                    Log.e("LOC", lastLocation.longitude.toString())
//                    Log.e("LOC", lastLocation.latitude.toString())
//                    val user = User("", "", "", "", "", "", "", newGeo)
//                    onUpdateHomeLocation(user)
//                }
//            }
//            R.id.nav_slideshow -> {
////                onGetAllReport()
//                onGetNearbyReports()
//            }
//            R.id.nav_manage -> {
//
//            }
//            R.id.nav_share -> {
//
//            }
//            R.id.nav_signout -> {
//                onSignOut()
//            }
//        }
//
//        drawer_layout.closeDrawer(GravityCompat.START)
//        return true
//    }

    private var currentSelectedPlaceMarker: Marker? = null
    fun addMarker(p: Place) {
        val markerOptions = MarkerOptions()
        markerOptions.position(p.latLng)
        markerOptions.title(p.name.toString() + "")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

//        mMap.addMarker(markerOptions)
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(p.latLng))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(17f))

        currentSelectedPlaceMarker = mMap.addMarker(markerOptions)
        currentSelectedPlaceMarker?.title = "current_place"
    }


    // ================================================================================================================================================= //
    // ======== VỀ THÔNG TIN NGƯỜI DÙNG USER =========================================================================================================== //
    // ================================================================================================================================================= //
    private fun loadUserProfile() {
        if (AppController.accessToken != null && AppController.accessToken.toString().length > 0) {
            val service = APIServiceGenerator.createService(UserService::class.java)
            val call = service.userProfile
            call.enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.isSuccessful) {
                        AppController.userProfile = response.body()!!.user
                        updateInformation()
                    } else {
                        val apiError = ErrorUtils.parseError(response)
                        TastyToast.makeText(this@MainActivity, apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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


        if (user!!.latHomeLocation != null && user.longHomeLocation != null) {
            // Lấy địa chỉ nhà sử dụng Geocoder
            val geocoder = Geocoder(this, Locale.getDefault())
            val yourAddresses: List<Address>
            yourAddresses = geocoder.getFromLocation(user.latHomeLocation!!, user.longHomeLocation!!, 1)

//            Toast.makeText(this, "MENU lat: " + user.latHomeLocation + " long: " + user.longHomeLocation, Toast.LENGTH_SHORT).show()
//            Log.e("LAT LONG", "MENU lat: " + user.latHomeLocation + " long: " + user.longHomeLocation)
//
            if (yourAddresses.isNotEmpty()) {
//            val yourAddress = yourAddresses.get(0).getAddressLine(0)
//            val yourCity = yourAddresses.get(0).getAddressLine(1)
//            val yourCountry = yourAddresses.get(0).getAddressLine(2)
                val address = yourAddresses.get(0).thoroughfare + ", " + yourAddresses.get(0).locality + ", " + yourAddresses.get(0).subAdminArea + ", " + yourAddresses.get(0).adminArea + ", " + yourAddresses.get(0).countryName
                tvAddressHome_menu.text = address
            }
        }

        if (user!!.latWorkLocation != null && user.longWorkLocation != null) {
            // Lấy địa chỉ nhà sử dụng Geocoder
            val geocoder = Geocoder(this, Locale.getDefault())
            val yourAddresses: List<Address>
            yourAddresses = geocoder.getFromLocation(user.latWorkLocation!!, user.longWorkLocation!!, 1)

            if (yourAddresses.isNotEmpty()) {
                val address = yourAddresses.get(0).thoroughfare + ", " + yourAddresses.get(0).locality + ", " + yourAddresses.get(0).subAdminArea + ", " + yourAddresses.get(0).adminArea + ", " + yourAddresses.get(0).countryName
                tvAddressWork_menu.text = address
            }
        }

//        Helper.loadAvatarWithoutPlaceHolder(getActivity(), avatar, user.getAvatar(), net.diadiemmuasam.user.R.drawable.default_avatar)
    }

    private fun onSignOut() {
        AppController.signOut()
        TastyToast.makeText(this, "Đăng xuất thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        this.finish()
    }


    // ================================================================================================================================================= //
    // ======== VỂ CLICK MARKER ======================================================================================================================== //
    // ================================================================================================================================================= //
    override fun onMarkerClick(p0: Marker): Boolean {
//        p0.showInfoWindow()

        if (p0.title == "current_place") {
            if (!isPlaceInfoWindowUp && currentSelectedPlace != null) {
                showPlaceInfoPopup(currentSelectedPlace!!)
            }
        }

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
            val imvRecord = viewReportPopup.findViewById<ImageView>(R.id.imRecord_marker_report)
            val imvImage = viewReportPopup.findViewById<ImageView>(R.id.imImage_marker_report)

            val dataReport: Report = marker.tag as Report
//            if (dataReport.subtype2 == "") {
//                tvType.text = dataReport.subtype1
//            } else {
//                tvType.text = dataReport.subtype2
//            }

            // Làm tròn số double
            val decimalFormat = DecimalFormat("#")
            decimalFormat.roundingMode = RoundingMode.CEILING

            tvDistance.text = "Cách " + decimalFormat.format(dataReport.distance) + " m"

            // Lấy địa chỉ sử dụng Geocoder
            val geocoder = Geocoder(this, Locale.getDefault())
            val yourAddresses: List<Address>
            yourAddresses = geocoder.getFromLocation(dataReport.geometry!!.coordinates!![1], dataReport.geometry!!.coordinates!![0], 1)

            if (yourAddresses.isNotEmpty()) {
//                val yourAddress = yourAddresses.get(0).getAddressLine(0)
//                val yourCity = yourAddresses.get(0).getAddressLine(1)
//                val yourCountry = yourAddresses.get(0).getAddressLine(2)
                val address = yourAddresses.get(0).thoroughfare + ", " + yourAddresses.get(0).locality + ", " + yourAddresses.get(0).subAdminArea
                tvLocation.text = address
            }

            tvDescription.text = dataReport.description.toString()
            when (dataReport.type) {
                "traffic" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_traffic)
                    when (dataReport.subtype1) {
                        "moderate" -> {
                            imvType.setImageResource(R.drawable.ic_report_traffic_moderate)
                            tvType.text = "Kẹt xe vừa"
                        }
                        "heavy" -> {
                            imvType.setImageResource(R.drawable.ic_report_traffic_heavy)
                            tvType.text = "Kẹt xe nặng"
                        }
                        "standstill" -> {
                            imvType.setImageResource(R.drawable.ic_report_traffic_standstill)
                            tvType.text = "Kẹt cứng"
                        }
                    }
                }
                "crash" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_crash)
                    when (dataReport.subtype1) {
                        "minor" -> {
                            imvType.setImageResource(R.drawable.ic_accident_minor)
                            tvType.text = "Tai nạn nhỏ"
                        }
                        "major" -> {
                            imvType.setImageResource(R.drawable.ic_accident_major)
                            tvType.text = "Tai nạn nghiêm trọng"
                        }
                        "other_side" -> {
                            imvType.setImageResource(R.drawable.ic_accident_other_side)
                            tvType.text = "Tai nạn bên đường"
                        }
                    }
                }
                "hazard" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_hazard)
                    when (dataReport.subtype2) {
                        "object" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_object)
                            tvType.text = "Vật cản"
                        }
                        "construction" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_construction)
                            tvType.text = "Công trình"
                        }
                        "broken_light" -> {
                            imvType.setImageResource(R.drawable.ic_report_broken_traffic_light)
                            tvType.text = "Đèn giao thông hư"
                        }
                        "pothole" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_pothole)
                            tvType.text = "Hố voi"
                        }
                        "vehicle_stop" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_stopped)
                            if (dataReport.subtype1 == "on_road") {
                                tvType.text = "Xe đậu"
                            }
                            if (dataReport.subtype1 == "shoulder") {
                                tvType.text = "Xe đậu bên lề"
                            }
                        }
                        "road_kill" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_roadkill)
                            tvType.text = "Động vật chết"
                        }
                        "animal" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_animals)
                            tvType.text = "Động vật qua đường"
                        }
                        "missing_sign" -> {
                            imvType.setImageResource(R.drawable.ic_report_hazard_missingsign)
                            tvType.text = "Thiếu biển báo"
                        }
                        "fog" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_fog)
                            tvType.text = "Sương mù"
                        }
                        "hail" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_hail)
                            tvType.text = "Mưa đá"
                        }
                        "flood" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_flood)
                            tvType.text = "Lũ lụt"
                        }
                        "ice" -> {
                            imvType.setImageResource(R.drawable.ic_hazard_weather_ice)
                            tvType.text = "Đá trơn"
                        }
                    }
                }
                "help" -> {
                    imvType.background = getDrawable(R.drawable.bg_btn_report_assistance)
                    when (dataReport.subtype2) {
                        "no_gas" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_no_gas)
                            tvType.text = "Hết xăng"
                        }
                        "flat_tire" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_flat_tire)
                            tvType.text = "Xẹp lốp xe"
                        }
                        "no_battery" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_no_battery)
                            tvType.text = "Hết bình"
                        }
                        "medical_care" -> {
                            imvType.setImageResource(R.drawable.ic_report_sos_medical_care)
                            tvType.text = "Chăm sóc y tế"
                        }
                    }
                }
            }
            curMarkerReport = marker
            imvUpVote.setOnClickListener {
                viewReportPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                Toast.makeText(this, "Up Vote", Toast.LENGTH_SHORT).show()
                mPopupWindowReport!!.dismiss()
                curMarkerReport = null
            }

            // Nếu là người sở hữu, sửa nút DownVote thành nút Xoá
            var isDelete = false
            if (AppController.userProfile!!._id == dataReport.userID) {
                imvDownVote.setImageResource(R.drawable.ic_delete_white)
                isDelete = true
            }
            imvDownVote.setOnClickListener {
                viewReportPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                if (isDelete) {
                    Toast.makeText(this, "Remove Report", Toast.LENGTH_SHORT).show()
                    mPopupWindowReport!!.dismiss()
                    curMarkerReport = null
                    onDeleteReport(dataReport._id.toString())
                } else {
                    Toast.makeText(this, "Down Vote", Toast.LENGTH_SHORT).show()
                    mPopupWindowReport!!.dismiss()
                    curMarkerReport = null
                }
            }
            imvRecord.setOnClickListener {
                //                val filePath = externalCacheDir!!.absolutePath + "/" + dataReport._id.toString() + ".3gp"
                if (dataReport.byteAudioFile != "") {
                    val filePath = externalCacheDir!!.absolutePath + "/audio_decoded.3gp"
                    FileUtils.decodeAudioFile(dataReport.byteAudioFile!!, filePath)
                } else {
                    TastyToast.makeText(this, "Không có dữ liệu thu âm", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                }
            }
            imvImage.setOnClickListener {
                if (dataReport.byteImageFile != "") {
                    val intent = Intent(this, CustomCameraActivity::class.java)
                    intent.putExtra("base64Image", dataReport.byteImageFile)
                    startActivity(intent)
                } else {
                    TastyToast.makeText(this, "Không có dữ liệu hình ảnh", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                }
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

            val btnConfirm = viewUserPopup.findViewById<LinearLayout>(R.id.layoutConfirm_marker_user)
            val imvType = viewUserPopup.findViewById<ImageView>(R.id.imvType_marker_user)
            val tvType = viewUserPopup.findViewById<TextView>(R.id.tvType_marker_user)

            val dataUser: User = marker.tag as User
            tvName.text = dataUser.name.toString()
//            tvDOB.text = dataUser.birthDate.toString()
            tvEmail.text = dataUser.email.toString()
            btnConfirm.visibility = View.INVISIBLE

            curMarkerUser = marker
            btnHello.setOnClickListener {
                attemptHello(AppController.userProfile?.name.toString(), dataUser.socketID.toString())
                mPopupWindowUser!!.dismiss()
                curMarkerUser = null
                viewUserPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

            // Lấy view của viewTouch
            val viewTouch = viewUserPopup.findViewById<View>(R.id.viewTouch_maker_user)
            val sfg = SimpleFingerGestures()
            sfg.setDebug(true)
            sfg.consumeTouchEvents = true

            var mType: Number = 0

            sfg.setOnFingerGestureListener(object : SimpleFingerGestures.OnFingerGestureListener {
                override fun onDoubleTap(fingers: Int): Boolean {
//                    Toast.makeText(this@MainActivity, "You double tap", Toast.LENGTH_SHORT).show()
                    btnConfirm.visibility = View.VISIBLE
                    //==== Ban ngày
                    // Chưa biết

                    //==== Ban đêm
                    // Nháy báo hiệu xe ngược chiều giảm độ sáng đèn pha
                    mType = 1
                    imvType.setImageResource(R.drawable.ic_headlights_on_44dp)
                    tvType.text = "HẠ ĐỘ SÁNG ĐÈN PHA"
//                    btnConfirm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_headlights_on_44dp,0, 0)

                    // return true thì sẽ cho phép các gesture khác cũng bắt được sự kiện đó
                    // return false thì chỉ sự kiện nào bắt được đúng sự iện đó và không gửi đi tiếp
                    return false
                }

                override fun onPinch(fingers: Int, gestureDuration: Long, gestureDistance: Double): Boolean {
//                    Toast.makeText(this@MainActivity, "You pinched " + fingers + " fingers " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
                    btnConfirm.visibility = View.INVISIBLE
                    mType = 0
                    return false
                }

                override fun onUnpinch(fingers: Int, gestureDuration: Long, gestureDistance: Double): Boolean {
//                    Toast.makeText(this@MainActivity, "You unpinched " + fingers + " fingers " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
                    btnConfirm.visibility = View.INVISIBLE
                    mType = 0
                    return false
                }

                override fun onSwipeDown(fingers: Int, gestureDuration: Long, gestureDistance: Double): Boolean {
//                    Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  down " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()

//                    if(fingers == 2 && gestureDistance <= 100){
//                        Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers down " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    }

                    btnConfirm.visibility = View.INVISIBLE
                    mType = 0
                    if (fingers == 2 && gestureDistance >= 120) {
                        // Báo giảm tốc độ
                        btnConfirm.visibility = View.VISIBLE
                        mType = 2
                        imvType.setImageResource(R.drawable.ic_report_hazard_44dp)
                        tvType.text = "NGUY HIỂM NÊN GIẢM TỐC ĐỘ"
                    }
                    if (fingers == 3 && gestureDistance >= 120) {
                        // Báo có công an
                        btnConfirm.visibility = View.VISIBLE
                        mType = 3
//                        imvType.setImageResource(R.drawable.ic_report_watcher_44dp)
                        imvType.setImageResource(R.drawable.ic_report_camera_trafficlight_44dp)
                        tvType.text = "CÓ GIÁM SÁT GẦN ĐÓ"
                    }
                    if (fingers == 4 && gestureDistance >= 120) {
                        // Báo nên quay đầu
                        btnConfirm.visibility = View.VISIBLE
                        mType = 4
                        imvType.setImageResource(R.drawable.ic_report_turn_around_44dp)
                        tvType.text = "NGUY HIỂM NÊN QUAY ĐẦU"
                    }
                    return false
                }

                override fun onSwipeUp(fingers: Int, gestureDuration: Long, gestureDistance: Double): Boolean {
//                    Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  up " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    if(fingers == 2 && gestureDistance <= 100){
//                        Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  up " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    }
                    btnConfirm.visibility = View.INVISIBLE
                    mType = 0
                    return false
                }

                override fun onSwipeLeft(fingers: Int, gestureDuration: Long, gestureDistance: Double): Boolean {
//                    Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  left " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    if(fingers == 2 && gestureDistance <= 100){
//                        Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  left " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    }
                    btnConfirm.visibility = View.INVISIBLE
                    mType = 0
                    return false
                }

                override fun onSwipeRight(fingers: Int, gestureDuration: Long, gestureDistance: Double): Boolean {
//                    Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  right " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    if(fingers == 2 && gestureDistance <= 100){
//                        Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  right " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    }
                    btnConfirm.visibility = View.INVISIBLE
                    mType = 0
                    return false
                }
            })

            viewTouch.setOnTouchListener(sfg)

            btnConfirm.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                when (mType) {
                    1 -> {
                        attemptWarnStrongLight(AppController.userProfile?.name.toString(), dataUser.socketID.toString())
                    }
                    2 -> {
                        attemptWarnSlowDown(AppController.userProfile?.name.toString(), dataUser.socketID.toString())
                    }
                    3 -> {
                        attemptWarnWatcher(AppController.userProfile?.name.toString(), dataUser.socketID.toString())
                    }
                    4 -> {
                        attemptWarnTurnAround(AppController.userProfile?.name.toString(), dataUser.socketID.toString())
                    }
                }
                TastyToast.makeText(this, "Đã gửi cảnh báo cho tài xế", TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show()
                btnConfirm.visibility = View.INVISIBLE
                mType = 0
            }


//            // Set GestureDetector
//            mDetector = GestureDetector(this, CustomGestureDetector())
//            viewTouch.setOnTouchListener(object : View.OnTouchListener {
//                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    mDetector.onTouchEvent(event)
//                    return true
//                }
//            })

        }
    }

//    // Override onTouch để lấy event
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        mDetector.onTouchEvent(event)
//        return super.onTouchEvent(event)
//    }

    // Sự kiện khi click vào info windows
    override fun onInfoWindowClick(p0: Marker) {
    }

    override fun onInfoWindowClose(p0: Marker?) {
        if (p0?.title == "report") {
            // Đóng popup windows
            mPopupWindowReport?.dismiss()
            curMarkerReport = null
        }
        if (p0?.title == "user") {
            mPopupWindowUser?.dismiss()
            curMarkerUser = null
        }
//        if (p0?.title == "current_place") {
//            mPopupWindowPlaceInfo?.dismiss()
//        }

    }

//    private fun moveMarker(marker: MarkerOptions, latLng: LatLng) {
//        marker.position(latLng)
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
//    }

//    private fun getUserFromMarker(marker: Marker): User {
//        val listGeo: List<Double> = listOf(0.0, 0.0)
//        val newGeo = Geometry("Point", listGeo)
//        var user = User("", "", "", "", "", "", "", newGeo)
//        for (i in 0 until (listUser.size)) {
//            // Except current user
//            if (listUser[i].email == marker.snippet) {
//                user = listUser[i]
//            }
//        }
//        return user
//    }


    // ====================================================================================================================================================== //
    // ======== VỀ GỌI API VÀ LISTENER USER ================================================================================================================= //
    // ====================================================================================================================================================== //
    private fun onGetAllUser() {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.allUserProfile
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    onAllUserProfileSuccess(response.body()!!)
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                t.printStackTrace()
            }
        })
    }

    private fun onAllUserProfileSuccess(response: List<User>) {
        listUser = response

        if (AppController.settingFilterCar == "true") {
            drawValidUsers()
        }
    }

    private fun onGetNearbyUsers() {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.getNearbyUsers(lastLocation.latitude, lastLocation.longitude, 10000f)
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    // Toast.makeText(this@MainActivity, "Phạm vi 3 km", Toast.LENGTH_SHORT).show()
                    onAllUserProfileSuccess(response.body()!!)
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                t.printStackTrace()
            }
        })
    }

    private fun drawValidUsers() {
        if (listUser.size == 1) {
            Toast.makeText(this, "Không tìm thấy xe khác", Toast.LENGTH_SHORT).show()
        } else {
            if (curMarkerUser == null || listUserMarker.size == 0) {
                for (i in 0 until listUserMarker.size) {
                    listUserMarker[i].remove()
                }
                listUserMarker.clear()
                for (i in 0 until (listUser.size)) {
                    if (listUser[i].email != AppController.userProfile!!.email) {
                        addUserMarker(listUser[i])
                    }
                }
            }
            if (curMarkerUser != null) {
                val size = listUserMarker.size
                for (i in (size - 1) downTo 0) {
                    if (curMarkerUser!!.id != listUserMarker[i].id) {
                        listUserMarker[i].remove()
                        listUserMarker.removeAt(i)
                    }
                }
                val dataUser = curMarkerUser!!.tag as User
                for (i in 0 until (listUser.size)) {
                    if (listUser[i].email != AppController.userProfile!!.email && listUser[i]._id != dataUser._id) {
                        addUserMarker(listUser[i])
                    }
                }
            }
        }
    }

    private fun addUserMarker(user: User) {
        val markerOptions = MarkerOptions()
        // LatLag điền theo thứ tự latitude, longitude
        // Còn ở server Geo là theo thứ tự longitude, latitude
        // Random
        val random = Random()
        markerOptions.position(LatLng(user.currentLocation!!.coordinates!![1], user.currentLocation!!.coordinates!![0]))
        markerOptions.title("user")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(random.nextFloat() * 360))
        val marker = mMap.addMarker(markerOptions)
        listUserMarker.add(marker)
        marker.tag = user
    }

    private fun onUpdateCurrentLocation(user: User) {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.updateCurrentLocation(user)
        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
//                     Toast.makeText(this@MainActivity, "Vị trí mới: " + "long:" + response.body()!!.user?.currentLocation?.coordinates!![0] + "- lat: " + response.body()!!.user?.currentLocation?.coordinates!![1], Toast.LENGTH_SHORT).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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
                    // Đã update vào trong AppController
                    // Toast.makeText(this@MainActivity, "Socket ID hiện tại: " + response.body().user?.socketID, Toast.LENGTH_SHORT).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

    private fun onUpdateHomeLocation(user: User) {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.updateHomeLocation(user)
        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    // Đã update vào AppController ở HomeSettingActivity
//                     Toast.makeText(this@MainActivity, "Vị trí mới: " + "long:" + response.body()!!.user?.currentLocation?.coordinates!![0] + "- lat: " + response.body()!!.user?.currentLocation?.coordinates!![1], Toast.LENGTH_SHORT).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

    private fun onUpdateWorkLocation(user: User) {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.updateWorkLocation(user)
        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
//                     Toast.makeText(this@MainActivity, "Vị trí mới: " + "long:" + response.body()!!.user?.currentLocation?.coordinates!![0] + "- lat: " + response.body()!!.user?.currentLocation?.coordinates!![1], Toast.LENGTH_SHORT).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }


    // ======================================================================================================================================================== //
    // ======== SOCKET EVENT ================================================================================================================================== //
    // ======================================================================================================================================================== //
    private fun initSocket() {
        socket = SocketService().getSocket()
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
//        socket.on("chat message", onNewMessage)
        socket.on("event_hello_socket", onSayHello)
        socket.on("event_warn_strong_light_socket", onWarnStrongLight)
        socket.on("event_warn_watcher_socket", onWarnWatcher)
        socket.on("event_warn_slow_down_socket", onWarnSlowDown)
        socket.on("event_warn_turn_around_socket", onWarnTurnAround)
        socket.on("event_warn_thank_socket", onWarnThank)

        socket.on("event_report_other_socket", onReportOther)
        socket.connect()
    }

    private fun destroySocket() {
        socket.off(Socket.EVENT_CONNECT, onConnect)
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
//        socket.on("chat message", onNewMessage)
        socket.off("event_hello_socket", onSayHello)
        socket.off("event_warn_strong_light_socket", onWarnStrongLight)
        socket.off("event_warn_watcher_socket", onWarnWatcher)
        socket.off("event_warn_slow_down_socket", onWarnSlowDown)
        socket.off("event_warn_turn_around_socket", onWarnTurnAround)
        socket.off("event_warn_thank_socket", onWarnThank)

        socket.off("event_report_other_socket", onReportOther)
        socket.disconnect()
    }

    private val onConnect = Emitter.Listener {
        this.runOnUiThread {
            TastyToast.makeText(this.applicationContext,
                    "Đã kết nối socket", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
            // Gán socket ID vào cho socketID của người dùng
            AppController.userProfile?.socketID = socket.id()
            onUpdateSocketID(AppController.userProfile!!)
        }
    }

    private val onDisconnect = Emitter.Listener {
        this.runOnUiThread {
            TastyToast.makeText(this.applicationContext,
                    "Ngắt kết nối socket", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
        }
    }

    private val onConnectError = Emitter.Listener {
        this.runOnUiThread {
            TastyToast.makeText(this.applicationContext,
                    "Lỗi kết nối socket", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
        }
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
            val email: String
            val sendID: String
            val message: String
            try {
                email = args[0] as String
                sendID = args[1] as String
                message = args[2] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (message == "hello") {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewHelloPopup = inflater.inflate(R.layout.hello_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewHelloPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewHelloPopup.findViewById<TextView>(R.id.tvEmail_hello_dialog)
                val btnHello = viewHelloPopup.findViewById<Button>(R.id.btnHello_hello_dialog)
                val imImage = viewHelloPopup.findViewById<ImageView>(R.id.imImage_hello_dialog)

                tvEmail.text = email

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                object : CountDownTimer(3000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                        btnHello.text = String.format(Locale.getDefault(), "%s (%d)",
                                "CHÀO LẠI",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
                        viewHelloPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }

                    override fun onFinish() {
                        mPopupWindowHello!!.dismiss()
                    }
                }.start()

                btnHello.setOnClickListener {
                    attemptHello(AppController.userProfile?.email.toString(), sendID)
                    mPopupWindowHello!!.dismiss()
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }

//                val factory = LayoutInflater.from(this)
//                val customDialogView = factory.inflate(R.layout.hello_dialog_layout, null)
//                val customDialog = AlertDialog.Builder(this).create()
//                customDialog.setView(customDialogView)
//                customDialog.setOnShowListener(DialogInterface.OnShowListener {
//                    customDialog.findViewById<TextView>(R.id.tvEmail_hello_dialog).text = email
//                    object : CountDownTimer(2000, 500) {
//
//                        override fun onTick(millisUntilFinished: Long) {
//                            customDialogView.findViewById<Button>(R.id.btnHello_hello_dialog).text = String.format(Locale.getDefault(), "%s (%d)",
//                                    "CHÀO LẠI",
//                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
//                        }
//
//                        override fun onFinish() {
//                            customDialog.dismiss()
//                        }
//                    }.start()
//                })
//
//                customDialogView.findViewById<Button>(R.id.btnHello_hello_dialog).setOnClickListener {
//                    attemptHello(AppController.userProfile?.name.toString(), sendID)
//                    customDialog.dismiss()
//                }
//                customDialog.show()
            }
        })
    }

    private fun attemptHello(email: String, receiveSocketID: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("event_hello_server", email, socket.id(), receiveSocketID, "hello")
    }

    private val onWarnStrongLight = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {
            //            val data : JSONObject = args[0] as JSONObject
            val email: String
            val sendID: String
            val message: String
            try {
                email = args[0] as String
                sendID = args[1] as String
                message = args[2] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (message == "strong light") {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnStrongLightPopup = inflater.inflate(R.layout.warn_strong_light_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewWarnStrongLightPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewWarnStrongLightPopup.findViewById<TextView>(R.id.tvEmail_warn_strong_light_dialog)
                val btnThank = viewWarnStrongLightPopup.findViewById<Button>(R.id.btnThank_warn_strong_light_dialog)
                val imImage = viewWarnStrongLightPopup.findViewById<ImageView>(R.id.imImage_warn_strong_light_dialog)

                tvEmail.text = email

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                object : CountDownTimer(3000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                        btnThank.text = String.format(Locale.getDefault(), "%s (%d)",
                                "CẢM ƠN",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
                        viewWarnStrongLightPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }

                    override fun onFinish() {
                        mPopupWindowHello!!.dismiss()
                    }
                }.start()

                btnThank.setOnClickListener {
                    attemptWarnThank(AppController.userProfile?.name.toString(), sendID)
                    mPopupWindowHello!!.dismiss()
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
        })
    }

    private fun attemptWarnStrongLight(email: String, receiveSocketID: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("event_warn_strong_light_server", email, socket.id(), receiveSocketID, "strong light")
    }

    private val onWarnWatcher = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {
            //            val data : JSONObject = args[0] as JSONObject
            val email: String
            val sendID: String
            val message: String
            try {
                email = args[0] as String
                sendID = args[1] as String
                message = args[2] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (message == "watcher") {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnWatcherPopup = inflater.inflate(R.layout.warn_watcher_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewWarnWatcherPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewWarnWatcherPopup.findViewById<TextView>(R.id.tvEmail_warn_watcher_dialog)
                val btnThank = viewWarnWatcherPopup.findViewById<Button>(R.id.btnThank_warn_watcher_dialog)
                val imImage = viewWarnWatcherPopup.findViewById<ImageView>(R.id.imImage_warn_watcher_dialog)

                tvEmail.text = email

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                object : CountDownTimer(3000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                        btnThank.text = String.format(Locale.getDefault(), "%s (%d)",
                                "CẢM ƠN",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
                        viewWarnWatcherPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }

                    override fun onFinish() {
                        mPopupWindowHello!!.dismiss()
                    }
                }.start()

                btnThank.setOnClickListener {
                    attemptWarnThank(AppController.userProfile?.name.toString(), sendID)
                    mPopupWindowHello!!.dismiss()
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
        })
    }

    private fun attemptWarnWatcher(email: String, receiveSocketID: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("event_warn_watcher_server", email, socket.id(), receiveSocketID, "watcher")
    }

    private val onWarnSlowDown = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {
            //            val data : JSONObject = args[0] as JSONObject
            val email: String
            val sendID: String
            val message: String
            try {
                email = args[0] as String
                sendID = args[1] as String
                message = args[2] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (message == "slow down") {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnSlowDownPopup = inflater.inflate(R.layout.warn_slow_down_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewWarnSlowDownPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewWarnSlowDownPopup.findViewById<TextView>(R.id.tvEmail_warn_slow_down_dialog)
                val btnThank = viewWarnSlowDownPopup.findViewById<Button>(R.id.btnThank_warn_slow_down_dialog)
                val imImage = viewWarnSlowDownPopup.findViewById<ImageView>(R.id.imImage_warn_slow_down_dialog)

                tvEmail.text = email

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                object : CountDownTimer(3000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                        btnThank.text = String.format(Locale.getDefault(), "%s (%d)",
                                "CẢM ƠN",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
                        viewWarnSlowDownPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }

                    override fun onFinish() {
                        mPopupWindowHello!!.dismiss()
                    }
                }.start()

                btnThank.setOnClickListener {
                    attemptWarnThank(AppController.userProfile?.name.toString(), sendID)
                    mPopupWindowHello!!.dismiss()
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
        })
    }

    private fun attemptWarnSlowDown(email: String, receiveSocketID: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("event_warn_slow_down_server", email, socket.id(), receiveSocketID, "slow down")
    }

    private val onWarnTurnAround = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {
            //            val data : JSONObject = args[0] as JSONObject
            val email: String
            val sendID: String
            val message: String
            try {
                email = args[0] as String
                sendID = args[1] as String
                message = args[2] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (message == "turn around") {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnTurnAroundPopup = inflater.inflate(R.layout.warn_turn_around_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewWarnTurnAroundPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewWarnTurnAroundPopup.findViewById<TextView>(R.id.tvEmail_warn_turn_around_dialog)
                val btnThank = viewWarnTurnAroundPopup.findViewById<Button>(R.id.btnThank_warn_turn_around_dialog)
                val imImage = viewWarnTurnAroundPopup.findViewById<ImageView>(R.id.imImage_warn_turn_around_dialog)

                tvEmail.text = email

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                object : CountDownTimer(3000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                        btnThank.text = String.format(Locale.getDefault(), "%s (%d)",
                                "CẢM ƠN",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
                        viewWarnTurnAroundPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }

                    override fun onFinish() {
                        mPopupWindowHello!!.dismiss()
                    }
                }.start()

                btnThank.setOnClickListener {
                    attemptWarnThank(AppController.userProfile?.name.toString(), sendID)
                    mPopupWindowHello!!.dismiss()
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
        })
    }

    private fun attemptWarnTurnAround(email: String, receiveSocketID: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("event_warn_turn_around_server", email, socket.id(), receiveSocketID, "turn around")
    }

    private val onWarnThank = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {
            //            val data : JSONObject = args[0] as JSONObject
            val email: String
            val sendID: String
            val message: String
            try {
                email = args[0] as String
                sendID = args[1] as String
                message = args[2] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (message == "thank") {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnThankPopup = inflater.inflate(R.layout.warn_thank_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewWarnThankPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewWarnThankPopup.findViewById<TextView>(R.id.tvEmail_warn_thank_dialog)
                val imImage = viewWarnThankPopup.findViewById<ImageView>(R.id.imImage_warn_thank_dialog)

                tvEmail.text = email

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                object : CountDownTimer(3000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        mPopupWindowHello!!.dismiss()
                    }
                }.start()
            }
        })
    }

    private fun attemptWarnThank(email: String, receiveSocketID: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("event_warn_thank_server", email, socket.id(), receiveSocketID, "thank")
    }

    private val onReportOther = Emitter.Listener { args ->
        this.runOnUiThread(Runnable {
            //            val data : JSONObject = args[0] as JSONObject
//            Toast.makeText(this, "Vô chỗ nhận rồi", Toast.LENGTH_SHORT).show()
            val email: String
            val sendID: String
            val type: String
            val base64Image: String
            val licensePLate: String
            try {
                email = args[0] as String
                sendID = args[1] as String
                type = args[2] as String
                base64Image = args[3] as String
                licensePLate = args[4] as String

            } catch (e: JSONException) {
                Log.e("LOG", e.message)
                return@Runnable
            }
            if (type == "careless_driver") {
                Toast.makeText(this, "Đã vào", Toast.LENGTH_SHORT).show()
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewReportOtherPopup = inflater.inflate(R.layout.report_other_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewReportOtherPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewReportOtherPopup.findViewById<TextView>(R.id.tvEmail_report_other_dialog)
                val imImage = viewReportOtherPopup.findViewById<ImageView>(R.id.imImage_report_other_dialog)
                val btnLicensePlate = viewReportOtherPopup.findViewById<Button>(R.id.btnLicensePlate_report_other_dialog)
                val imPicture = viewReportOtherPopup.findViewById<ImageView>(R.id.imPicture_report_other_dialog)
                val btnClose = viewReportOtherPopup.findViewById<Button>(R.id.btnClose_report_other_dialog)

                tvEmail.text = email

                btnLicensePlate.text = licensePLate

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                imPicture.setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    val intent = Intent(this, CustomCameraActivity::class.java)
                    intent.putExtra("base64Image", base64Image)
                    startActivity(intent)
                }

                btnClose.setOnClickListener {
                    mPopupWindowHello!!.dismiss()
                }
            }
            if (type == "piggy") {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewReportOtherPopup = inflater.inflate(R.layout.report_other_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewReportOtherPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewReportOtherPopup.findViewById<TextView>(R.id.tvEmail_report_other_dialog)
                val imImage = viewReportOtherPopup.findViewById<ImageView>(R.id.imImage_report_other_dialog)
                val btnLicensePlate = viewReportOtherPopup.findViewById<Button>(R.id.btnLicensePlate_report_other_dialog)
                val imPicture = viewReportOtherPopup.findViewById<ImageView>(R.id.imPicture_report_other_dialog)
                val btnClose = viewReportOtherPopup.findViewById<Button>(R.id.btnClose_report_other_dialog)
                val tvMess = viewReportOtherPopup.findViewById<TextView>(R.id.tvMess_report_other_dialog)

                tvEmail.text = email

                btnLicensePlate.text = licensePLate

                tvMess.text = "... CÓ XE CHIM MỒI"

                imImage.setImageResource(R.drawable.ic_piggy_bank)

                val animShake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
                imImage.startAnimation(animShake)

                imPicture.setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    val intent = Intent(this, CustomCameraActivity::class.java)
                    intent.putExtra("base64Image", base64Image)
                    startActivity(intent)
                }

                btnClose.setOnClickListener {
                    mPopupWindowHello!!.dismiss()
                }
            }
        })
    }

    private fun attemptReportOther(email: String, receiveSocketID: String, type: String, base64Image: String, licensePlate: String) {
        if (!socket.connected()) return

        // perform the sending message attempt.
        socket.emit("event_report_other_server", email, socket.id(), receiveSocketID, type, base64Image, licensePlate)
    }


    // ====================================================================================================================================================== //
    // ======== VỀ GỌI API VÀ LISTENER REPORT =============================================================================================================== //
    // ====================================================================================================================================================== //
    private fun onGetAllReport() {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.allReport
        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                if (response.isSuccessful) {
                    onAllReportSuccess(response.body()!!)
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                t.printStackTrace()
            }
        })
    }

    private fun onGetNearbyReports() {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.getNearbyReports(lastLocation.latitude, lastLocation.longitude, 10000f)
        call.enqueue(object : Callback<NearbyReportsResponse> {
            override fun onResponse(call: Call<NearbyReportsResponse>, response: Response<NearbyReportsResponse>) {
                if (response.isSuccessful) {
                    // Toast.makeText(this@MainActivity, "Phạm vi 3 km", Toast.LENGTH_SHORT).show()
                    onNearbyReportsSuccess(response.body()!!)
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<NearbyReportsResponse>, t: Throwable) {
                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
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

        for (i in 0 until (response.reports!!.size)) {
            listReport[i].distance = response.distances!![i]
        }

        if (AppController.settingFilterReport == "true") {
            drawValidReports()
        }

    }

    private fun drawValidReports() {
        if (curMarkerReport == null || listReportMarker.size == 0) {
            for (i in 0 until listReportMarker.size) {
                listReportMarker[i].remove()
            }
            listReportMarker.clear()
            for (i in 0 until (listReport.size)) {
                addReportMarker(listReport[i])
            }
        }
        if (curMarkerReport != null) {
            val size = listReportMarker.size
            for (i in (size - 1) downTo 0) {
                if (curMarkerReport!!.id != listReportMarker[i].id) {
                    listReportMarker[i].remove()
                    listReportMarker.removeAt(i)
                }
            }
            val dataReport = curMarkerReport!!.tag as Report
            for (i in 0 until (listReport.size)) {
                if (listReport[i]._id != dataReport._id) {
                    addReportMarker(listReport[i])
                }
            }
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
            "help" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_bar_report_assistance))
            }
        }
        val marker = mMap.addMarker(markerOptions)
        listReportMarker.add(marker)
        marker.tag = report
    }

    private fun onDeleteReport(reportID : String){
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.deleteReport(reportID)
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@MainActivity, "Xoá báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })

//        val service = APIServiceGenerator.createService(ReportService::class.java)
//        val call = service.deleteReport(reportID)
//        call.enqueue(object : Callback<SampleResponse> {
//            override fun onResponse(call: Call<SampleResponse>, response: Response<SampleResponse>) {
//                if (response.isSuccessful) {
//                    TastyToast.makeText(this@MainActivity, "Đã xoá báo cáo", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
//                } else {
//                    val apiError = ErrorUtils.parseError(response)
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
//                }
//            }
//
//            override fun onFailure(call: Call<SampleResponse>, t: Throwable) {
//                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
//                t.printStackTrace()
//            }
//        })
    }

    // ====================================================================================================================================================== //
    // ======== GESTURE DETECTOR ============================================================================================================================ //
    // ====================================================================================================================================================== //
//    private lateinit var mContext: Context

    class CustomGestureDetector : GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            Toast.makeText(mContext, "Custom: onDoubleTap", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            Toast.makeText(mContext, "Custom: onDoubleTapEvent", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            Toast.makeText(mContext, "Custom: onSingleTapConfirm", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onShowPress(e: MotionEvent?) {
            Toast.makeText(mContext, "Custom: onShowPress", Toast.LENGTH_SHORT).show()
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            Toast.makeText(mContext, "Custom: onSingleTap", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
            Toast.makeText(mContext, "Custom: onDown", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            Toast.makeText(mContext, "Custom: onFling", Toast.LENGTH_SHORT).show()
            if (e1!!.getX() < e2!!.getX()) {
                Log.d(TAG, "Left to Right swipe performed");
            }

            if (e1.getX() > e2.getX()) {
                Log.d(TAG, "Right to Left swipe performed");
            }

            if (e1.getY() < e2.getY()) {
                Log.d(TAG, "Up to Down swipe performed");
            }

            if (e1.getY() > e2.getY()) {
                Log.d(TAG, "Down to Up swipe performed");
            }

            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            Toast.makeText(mContext, "Custom: onScroll", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            Toast.makeText(mContext, "Custom: onLongPress", Toast.LENGTH_SHORT).show()
        }
    }
}
