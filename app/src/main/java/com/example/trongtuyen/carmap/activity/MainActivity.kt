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
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.*
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.activity.common.*
import com.example.trongtuyen.carmap.adapters.CustomInfoWindowAdapter
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.models.Geometry
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.models.direction.*
import com.example.trongtuyen.carmap.models.navigation.StepAdapter
import com.example.trongtuyen.carmap.models.nearbyplaces.NearbyPlacesInterface
import com.example.trongtuyen.carmap.models.nearbyplaces.NearbyPlacesResponse
import com.example.trongtuyen.carmap.services.*
import com.example.trongtuyen.carmap.services.models.NearbyReportsResponse
import com.example.trongtuyen.carmap.services.models.ReportResponse
import com.example.trongtuyen.carmap.services.models.UserProfileResponse
import com.example.trongtuyen.carmap.utils.AudioPlayer
import com.example.trongtuyen.carmap.utils.FileUtils
import com.example.trongtuyen.carmap.utils.Permission
import com.example.trongtuyen.carmap.utils.SharePrefs.Companion.mContext
import com.github.angads25.toggle.LabeledSwitch
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.maps.android.PolyUtil
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
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.UnsupportedEncodingException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener, View.OnClickListener, DirectionFinder.DirectionListener, GoogleMap.OnPolylineClickListener, OnStartDragListener {
    // Static variables
    companion object {
        // PERMISSION_REQUEST_CODE
        private const val MY_LOCATION_PERMISSION_REQUEST_CODE = 1
        // Log
        private const val TAG = "MainActivity"
        // Distance to determine whether a report is on Route or not
        private const val REPORT_ON_ROUTE_DISTANCE_DIRECTION = 5.0 // meter
        private const val REPORT_ON_ROUTE_DISTANCE_NAVIGATION = 20.0 // meter
        // Request code for activity result
        private const val PICK_PLACE_HISTORY_REQUEST = 4

        private const val REQUEST_CHECK_SETTINGS = 2
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

    private lateinit var mActionBarDrawerToggle: ActionBarDrawerToggle

    // Marker options for set up marker
    private var markerOptions = MarkerOptions()

    // Popup windows
    private var mPopupWindowReport: PopupWindow? = null

    private var mPopupWindowUser: PopupWindow? = null

    private var mPopupWindowHello: PopupWindow? = null

    private var mPopupWindowFilter: PopupWindow? = null

    private var mPopupWindowDelete: PopupWindow? = null

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

    private var listReportMarkerCurrentRoute: MutableList<Marker> = ArrayList()

    private var listReportCurrentRoute: MutableList<Report> = ArrayList()

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

    private var mPopupWindowRouteInfo: PopupWindow? = null

    private var mPopupWindowNavigationInfo: PopupWindow? = null

    private lateinit var viewNavigationPopup: View

    private var isPlaceInfoWindowUp = false

    private var currentSelectedPlace: Place? = null

    // Direction
    private lateinit var polylinePaths: MutableList<Polyline>
    private lateinit var currentPolyline: Polyline
    private lateinit var viewRoutePopup: View
    private var isRouteInfoWindowUp: Boolean = false
    private var isNavigationInfoWindowUp = false
    private var currentStepsLayout: RecyclerView? = null
    private var currentDirectionRoute: ArrayList<SimplePlace> = ArrayList<SimplePlace>()

    // setting hiện tại của socket
    private var currentSocketSetting: String? = null

    // setting hiện tại của status
//    private var currentStatusSetting: String? = null

    // AudioPlayer
    private var mAudioPlayer = AudioPlayer()

    // String step cũ để so sánh
    private lateinit var oldStep: Step

    // Settings
    private var isTouchSoundsEnabled: Boolean = false
    private var isTouchVibrateEnabled: Boolean = false

    // ==================================================================================================================================== //
    // ======== VỀ DIRECTION ============================================================================================================== //
    // ==================================================================================================================================== //
    @SuppressLint("InflateParams")
    private fun showPlaceInfoPopup(place: Place) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewPlacePopup = inflater.inflate(R.layout.place_info_layout, null)
        mPopupWindowPlaceInfo = PopupWindow(viewPlacePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mPopupWindowPlaceInfo!!.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)
        isPlaceInfoWindowUp = true

        val tvPlaceName = viewPlacePopup.findViewById<TextView>(R.id.tvPlaceName_place_info)
        val tvPlaceAddress = viewPlacePopup.findViewById<TextView>(R.id.tvPlaceAddress_place_info)
        val btnStartDirection = viewPlacePopup.findViewById<Button>(R.id.btnStartDirection_place_info)
        val btnSelectedPlace = viewPlacePopup.findViewById<LinearLayout>(R.id.btnSelectedPlace_place_info)

        tvPlaceName.text = place.name
        tvPlaceAddress.text = place.address

        btnStartDirection.setOnClickListener {
            currentDirectionRoute.clear()
            currentDirectionRoute.add(SimplePlace("Vị trí của bạn", LatLng(lastLocation.latitude, lastLocation.longitude)))
            currentDirectionRoute.add(SimplePlace(place.name.toString(), LatLng(place.latLng.latitude, place.latLng.longitude)))
            onBtnStartDirectionClick(currentDirectionRoute)
        }

        btnSelectedPlace.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 17f))
        }
//        imvReport.visibility = View.GONE
    }

    private fun onBtnStartDirectionClick(places: ArrayList<SimplePlace>) {
        if (places.size < 2)
            return
        if (isRouteInfoWindowUp)
            dismissPopupWindowRouteInfo()
        val origin = places[0].location!!.latitude.toString() + "," + places[0].location!!.longitude.toString()

        val destination = places[places.size - 1].location!!.latitude.toString() + "," + places[places.size - 1].location!!.longitude.toString()

        val waypoints = ArrayList<SimplePlace>()

        for (i in 1 until places.size - 1) {
            waypoints.add(places[i])
        }

        try {
            DirectionFinder(this, origin, destination, waypoints).execute()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    private fun removeCurrentDirectionPolyline() {
        if (::polylinePaths.isInitialized) {
            for (polyline in polylinePaths) {
                polyline.remove()
            }
            polylinePaths.clear()
        }

        for (marker in waypointsOnRouteMarkers) {
            marker.remove()
        }
        waypointsOnRouteMarkers.clear()
    }

    override fun onDirectionFinderStart() {
        removeCurrentDirectionPolyline()
    }

    private lateinit var viewDirectionPopup: View

    private lateinit var viewEditDirectionPopup: View

    private var mPopupWindowDirectionInfo: PopupWindow? = null

    private var mPopupWindowEditDirection: PopupWindow? = null

    private var isDirectionInfoWindowUp: Boolean = false

    private var isEditDirectionWindowUp: Boolean = false

//    private fun onDirectionChange(stops: ArrayList<SimplePlace>){
//        if (stops.size<2){
//            return
//        }
//        try {
//            val origin = stops[0].location!!.latitude.toString() + "," + stops[0].location!!.longitude.toString()
//
//            val destination = stops[stops.size-1].location!!.latitude.toString() + "," + stops[stops.size-1].location!!.longitude.toString()
//
//            stops.removeAt(stops.size-1)
//            stops.removeAt(0)
//
//            DirectionFinder(this, origin, destination, stops).execute()
//        } catch (e: UnsupportedEncodingException) {
//            e.printStackTrace()
//        }
//    }

    override fun onDirectionFinderSuccess(routes: List<Route>) {
        dismissPopupWindowPlaceInfo()
        // show Direction Info Popup
        showDirectionInfoPopup()

        polylinePaths = ArrayList()

        if (routes.size == 1 && routes[0].legs!!.size > 1) {
            var firstLeg = true
            for (leg in routes[0].legs!!) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(leg.startLocation, 16f))

                val polylineOptions = PolylineOptions().geodesic(true).width(10f).color(Color.GRAY)

                val polyline = drawPolyline(routes[0], polylineOptions, leg)

                if (firstLeg) {
                    firstLeg = false
                    currentPolyline = polyline
                    currentPolyline.zIndex = 1F
                    currentPolyline.color = Color.BLUE
                }
            }
            markWaypointsOnRoute(currentDirectionRoute)
            showRouteInfoPopup(routes[0])
        } else {
            var firstRoute = true
            for (route in routes) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16f))

                val polylineOptions = PolylineOptions().geodesic(true).width(10f).color(Color.GRAY)

                val polyline = drawPolyline(route, polylineOptions)

                if (firstRoute) {
                    firstRoute = false
                    currentPolyline = polyline
                    currentPolyline.zIndex = 1F
                    currentPolyline.color = Color.BLUE
                    showRouteInfoPopup(route)
                }
            }
            markWaypointsOnRoute(currentDirectionRoute)
        }

    }

    private fun showDirectionInfoPopup() {
        if (isDirectionInfoWindowUp)
            return
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewDirectionPopup = inflater.inflate(R.layout.direction_layout, null)
        mPopupWindowDirectionInfo = PopupWindow(viewDirectionPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mPopupWindowDirectionInfo!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)
        isDirectionInfoWindowUp = true

        val tvOrigin = viewDirectionPopup.findViewById<TextView>(R.id.tvOrigin_direction_layout)
        val tvWayPoints = viewDirectionPopup.findViewById<TextView>(R.id.tvWaypoints_direction_layout)
        val tvDestination = viewDirectionPopup.findViewById<TextView>(R.id.tvDestination_direction_layout)
        val btnEdit = viewDirectionPopup.findViewById<ImageView>(R.id.btnEdit_direction_layout)
        val btnBack = viewDirectionPopup.findViewById<ImageView>(R.id.btnBack_direction_layout)

        if (currentDirectionRoute.size > 1) {
            tvOrigin.text = "Từ: " + currentDirectionRoute[0].name
            if (currentDirectionRoute.size < 3) {
                tvWayPoints.visibility = View.GONE
            } else {
                tvWayPoints.visibility = View.VISIBLE
                tvWayPoints.text = "Qua: " + (currentDirectionRoute.size - 2).toString() + " điểm dừng"
            }

            tvDestination.text = "Đến: " + currentDirectionRoute[currentDirectionRoute.size - 1].name
        }

        btnEdit.setOnClickListener {
            dismissPopupWindowDirectionInfo()
            showEditDirectionPopup()
        }

        btnBack.setOnClickListener {
            if (::polylinePaths.isInitialized && polylinePaths.isNotEmpty() && isRouteInfoWindowUp && isDirectionInfoWindowUp) {
                removeCurrentDirectionPolyline()
                dismissPopupWindowRouteInfo()
                dismissPopupWindowDirectionInfo()
            }
        }
    }

    private fun showEditDirectionPopup() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewEditDirectionPopup = inflater.inflate(R.layout.edit_direction_layout, null)
        mPopupWindowEditDirection = PopupWindow(viewEditDirectionPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mPopupWindowEditDirection!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)
        isEditDirectionWindowUp = true

        val btnBack = viewEditDirectionPopup.findViewById<ImageView>(R.id.btnBack_edit_direction_layout)
        val btnDone = viewEditDirectionPopup.findViewById<TextView>(R.id.btnDone_edit_direction_layout)
//        val btnAdd = viewEditDirectionPopup.findViewById<ImageView>(R.id.btnAdd_edit_direction_layout)
        val btnAdd = viewEditDirectionPopup.findViewById<TextView>(R.id.btnAdd_edit_direction_layout)

//        val stopsOnRoute = ArrayList<SimplePlace>()
//        val origin = SimplePlace("Vị trí của bạn", LatLng(lastLocation.latitude,lastLocation.longitude))
//        stopsOnRoute.add(origin)
//
//        val wayPoints = ArrayList<SimplePlace>()
//
//        for (i in 0 until wayPoints.size) {
//            stopsOnRoute.add(wayPoints[i])
//        }
//
//        if (currentSelectedPlace!=null){
//            val destination = SimplePlace(currentSelectedPlace!!.name.toString(),LatLng(route.endLocation!!.latitude,route.endLocation!!.longitude))
//            stopsOnRoute.add(destination)
//        }

        initDirectionRecyclerView(currentDirectionRoute, viewEditDirectionPopup, btnAdd)

        btnBack.setOnClickListener {
            //            dismissPopupWindowEditDirection()
//            onBtnStartDirectionClick(currentDirectionRoute)
            onFinishEditDirection()
        }

        btnDone.setOnClickListener {
            //            dismissPopupWindowEditDirection()
//            onBtnStartDirectionClick(currentDirectionRoute)
            onFinishEditDirection()
        }
    }

    private fun onFinishEditDirection() {
        dismissPopupWindowEditDirection()
        if (nearbyPlacesResultMarkers.size > 0) {
            for (i in 0 until nearbyPlacesResultMarkers.size) {
                nearbyPlacesResultMarkers[i].remove()
            }
            nearbyPlacesResultMarkers.clear()
        }
        onBtnStartDirectionClick(currentDirectionRoute)
    }

    private lateinit var viewAddPlacePopup: View
    private var mPopupWindowAddPlace: PopupWindow? = null
    private var isAddPlaceWindowUp = false

    private fun dismissAddPlacePopup() {
        mPopupWindowAddPlace?.dismiss()
        isAddPlaceWindowUp = false
    }

    private fun showAddPlacePopup(myDataSet: ArrayList<SimplePlace>, adapter: PlaceAdapter) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (!::viewAddPlacePopup.isInitialized) {
            viewAddPlacePopup = inflater.inflate(R.layout.place_picker_layout, null)
        }
        mPopupWindowAddPlace = PopupWindow(viewAddPlacePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mPopupWindowAddPlace!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)
        isAddPlaceWindowUp = true

        val placeAutoComplete = fragmentManager.findFragmentById(R.id.place_autocomplete_place_picker_layout)
                as PlaceAutocompleteFragment
        val btnNearbyGasStations = viewAddPlacePopup.findViewById<LinearLayout>(R.id.btnNearByGasStations_place_picker_layout)
        val btnNearbyParkings = viewAddPlacePopup.findViewById<LinearLayout>(R.id.btnNearByParkings_place_picker_layout)
        val btnNearbyCoffeeShops = viewAddPlacePopup.findViewById<LinearLayout>(R.id.btnNearByCoffeeShops_place_picker_layout)
        val btnNearbyRestaurants = viewAddPlacePopup.findViewById<LinearLayout>(R.id.btnNearByRestaurants_place_picker_layout)

        placeAutoComplete.setText(null)
        placeAutoComplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Maps", "Place selected: " + place.name)
                myDataSet.add(SimplePlace(place.name.toString(), LatLng(place.latLng.latitude, place.latLng.longitude)))
                adapter.notifyDataSetChanged()
//                isAddPlaceWindowUp = false
//                mPopupWindowAddPlace?.dismiss()
                dismissAddPlacePopup()
            }

            override fun onError(status: Status) {
                Log.d("Maps", "An error occurred: $status")
            }
        })
        btnNearbyGasStations.setOnClickListener {
            getNearbyPlaces("gas_station", lastLocation, 1500)
            dismissAddPlacePopup()
        }
        btnNearbyParkings.setOnClickListener {
            getNearbyPlaces("parking", lastLocation, 1500)
            dismissAddPlacePopup()
        }
        btnNearbyCoffeeShops.setOnClickListener {
            getNearbyPlaces("cafe", lastLocation, 1500)
            dismissAddPlacePopup()
        }
        btnNearbyRestaurants.setOnClickListener {
            getNearbyPlaces("restaurant", lastLocation, 1500)
            dismissAddPlacePopup()
        }
    }

    private var waypointsOnRouteMarkers = ArrayList<Marker>()

    private fun markWaypointsOnRoute(places: ArrayList<SimplePlace>) {
        waypointsOnRouteMarkers.clear()
        for (i in 1 until places.size) {
            waypointsOnRouteMarkers.add(mMap.addMarker(MarkerOptions().position(LatLng(places[i].location!!.latitude, places[i].location!!.longitude))))
        }
        // THAY HÌNH MARKER DESTINATION
        // waypointsOnRouteMarkers[size] -> change image
    }

    private fun drawPolyline(route: Route, options: PolylineOptions): Polyline {
        for (point in route.points!!) {
            options.add(point)
        }

        val polyline = mMap.addPolyline(options)
        polyline.isClickable = true
        polyline.tag = route
        polylinePaths.add(polyline)

        return polyline
    }

    private fun drawPolyline(route: Route, options: PolylineOptions, leg: Leg): Polyline {
        for (step in leg.steps!!) {
            for (point in step.points!!) {
                options.add(point)
            }
        }
        val polyline = mMap.addPolyline(options)
        polyline.tag = route
        polylinePaths.add(polyline)

        return polyline
    }

    override fun onPolylineClick(p0: Polyline) {
        if (p0 == currentPolyline || isNavigationInfoWindowUp)
            return
        p0.color = Color.BLUE
        currentPolyline.color = Color.GRAY
        p0.zIndex = 1F
        currentPolyline.zIndex = 0F
        currentPolyline = p0
        val currentRoute = currentPolyline.tag as Route
//        dismissPopupWindowRouteInfo()
//        showRouteInfoPopup(currentRoute)
        updateUIRouteInfoPopup(currentRoute)
    }

    @SuppressLint("InflateParams")
    private fun showRouteInfoPopup(route: Route) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewRoutePopup = inflater.inflate(R.layout.steps_layout, null)
        mPopupWindowRouteInfo = PopupWindow(viewRoutePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        imvReport.visibility = View.GONE
        mPopupWindowRouteInfo!!.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)

        isRouteInfoWindowUp = true

        val tvRouteDuration = viewRoutePopup.findViewById<TextView>(R.id.tvDuration_route_info)
        val tvRouteDistance = viewRoutePopup.findViewById<TextView>(R.id.tvDistance_route_info)
        val tvReportCount = viewRoutePopup.findViewById<TextView>(R.id.tvNumReport_route_info)
        val btnStartNavigation = viewRoutePopup.findViewById<Button>(R.id.btnStartNavigation_route_info)
        val btnSteps = viewRoutePopup.findViewById<LinearLayout>(R.id.btnSteps_route_info)
        val tvBackToMap = viewRoutePopup.findViewById<TextView>(R.id.tvSteps_detail_route_info_layout)
        val dividerAboveRecyclerView = viewRoutePopup.findViewById<LinearLayout>(R.id.recycler_view_divider_steps_layout)
        val dividerAboveReportDetail = viewRoutePopup.findViewById<LinearLayout>(R.id.report_detail_divider_route_info_layout)

        if (currentDirectionRoute.size == 2) {
            tvRouteDuration.text = route.duration!!.text
            tvRouteDistance.text = route.distance!!.text
        } else {
            val seconds = route.duration!!.value.toLong()
            Log.d("TimeConvert", "text = " + route.duration!!.text)
            Log.d("TimeConvert", "second = " + seconds.toString())
            val numHour = TimeUnit.SECONDS.toHours(seconds).toInt()
            Log.d("TimeConvert", "numHour = $numHour")
            val numMinute = (TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds))).toInt()
            Log.d("TimeConvert", "numMinute = $numMinute")
            var convertedDuration = ""
            val convertedDistance = route.distance!!.value / 1000
            if (numHour > 0) {
                convertedDuration += numHour.toString() + " giờ "
            }
            convertedDuration += numMinute.toString() + " phút"

            tvRouteDuration.text = convertedDuration
            tvRouteDistance.text = convertedDistance.toString() + " km"
        }

        btnStartNavigation.setOnClickListener {
            onBtnStartNavigationClick(route)
        }

        btnSteps.setOnClickListener {
            val recyclerView = viewRoutePopup.findViewById<RecyclerView>(R.id.recycler_view_steps_layout)
//            val directionLayout = viewDirectionPopup.findViewById<ConstraintLayout>(R.id.root_direction_layout)
            if (recyclerView.visibility == View.GONE) {
//                dismissPopupWindowDirectionInfo()
//                directionLayout.visibility=View.GONE

                recyclerView.visibility = View.VISIBLE
                currentStepsLayout = recyclerView
                initStepRecyclerView(route, viewRoutePopup)

                val layoutReport = viewRoutePopup.findViewById<LinearLayout>(R.id.layoutReport_detail)
                layoutReport.visibility = View.GONE
                tvBackToMap.text = "QUAY LẠI BẢN ĐỒ"
                dividerAboveRecyclerView.visibility = View.VISIBLE
            } else {
//                directionLayout.visibility=View.VISIBLE
//                mPopupWindowDirectionInfo?.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)
                currentStepsLayout!!.visibility = View.GONE
                currentStepsLayout = null
                if (listReportMarkerCurrentRoute.size > 0) {
                    val layoutReport = viewRoutePopup.findViewById<LinearLayout>(R.id.layoutReport_detail)
                    layoutReport.visibility = View.VISIBLE
                }
                tvBackToMap.text = "CHI TIẾT CÁC BƯỚC"
                dividerAboveRecyclerView.visibility = View.GONE
            }
        }

        // Count report on route
        val currentRoute = currentPolyline.tag as Route
        listReportMarkerCurrentRoute = ArrayList()
        listReportCurrentRoute = ArrayList()

        for (i in 0 until listReportMarker.size) {
            if (PolyUtil.isLocationOnPath(LatLng(listReportMarker[i].position.latitude, listReportMarker[i].position.longitude), currentRoute.points, true, REPORT_ON_ROUTE_DISTANCE_DIRECTION)) {
                listReportMarkerCurrentRoute.add(listReportMarker[i])
            }
        }

        listReportMarkerCurrentRoute.sortedWith(compareBy { (it.tag as Report).distance })

        for (i in 0 until listReportMarkerCurrentRoute.size) {
            listReportCurrentRoute.add(listReportMarkerCurrentRoute[i].tag as Report)
        }

        val layoutReport = viewRoutePopup.findViewById<LinearLayout>(R.id.layoutReport_detail)

        Log.v("ReportCount", "NumReport = " + listReportMarkerCurrentRoute.size.toString())

        if (listReportMarkerCurrentRoute.size > 0) {
            dividerAboveReportDetail.visibility = View.VISIBLE
            tvReportCount.visibility = View.VISIBLE
            tvReportCount.text = listReportMarkerCurrentRoute.size.toString() + " báo hiệu"

            val btnPreviousReport = viewRoutePopup.findViewById<ImageView>(R.id.btnPrevious_report_detail)
            val btnNextReport = viewRoutePopup.findViewById<ImageView>(R.id.btnNext_report_detail)
            val btnCurrentReport = viewRoutePopup.findViewById<LinearLayout>(R.id.btnCurrent_report_detail)
            val progressBar = viewRoutePopup.findViewById<ProgressBar>(R.id.progressBar_report_detail)
            progressBar.max = listReportMarkerCurrentRoute.size
            if (listReportMarkerCurrentRoute.size == 1) {
                progressBar.visibility = View.GONE
                btnNextReport.visibility = View.INVISIBLE
                btnPreviousReport.visibility = View.INVISIBLE
            }
            progressBar.progress = 1

            var currentReportIndex = 0

            updateUIReportDetail(listReportCurrentRoute[currentReportIndex], viewRoutePopup)

            btnCurrentReport.setOnClickListener {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(listReportMarkerCurrentRoute[currentReportIndex].position))
            }

            btnPreviousReport.setOnClickListener {
                //                Toast.makeText(this,"onBtnPreviousReportClick",Toast.LENGTH_SHORT).show()
                if (currentReportIndex > 0) {
                    currentReportIndex--
                    progressBar.progress--
                    Log.v("ReportCount", "currentReportIndex = " + currentReportIndex.toString())

                    updateUIReportDetail(listReportCurrentRoute[currentReportIndex], viewRoutePopup)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(listReportMarkerCurrentRoute[currentReportIndex].position))
                }
            }
            btnNextReport.setOnClickListener {
                //                Toast.makeText(this,"onBtnNextReportClick",Toast.LENGTH_SHORT).show()
                if (currentReportIndex + 1 < listReportMarkerCurrentRoute.size) {
                    currentReportIndex++
                    progressBar.progress++
                    Log.v("ReportCount", "currentReportIndex = " + currentReportIndex.toString())

                    updateUIReportDetail(listReportCurrentRoute[currentReportIndex], viewRoutePopup)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(listReportMarkerCurrentRoute[currentReportIndex].position))
                }
            }
        } else {
            layoutReport.visibility = View.GONE
            dividerAboveReportDetail.visibility = View.GONE
        }
    }

    private fun updateUIRouteInfoPopup(route: Route) {
        if (!isRouteInfoWindowUp || !::viewRoutePopup.isInitialized)
            return
        val tvRouteDuration = viewRoutePopup.findViewById<TextView>(R.id.tvDuration_route_info)
        val tvRouteDistance = viewRoutePopup.findViewById<TextView>(R.id.tvDistance_route_info)
        val tvReportCount = viewRoutePopup.findViewById<TextView>(R.id.tvNumReport_route_info)
        val btnStartNavigation = viewRoutePopup.findViewById<Button>(R.id.btnStartNavigation_route_info)
        val btnSteps = viewRoutePopup.findViewById<LinearLayout>(R.id.btnSteps_route_info)
        val tvBackToMap = viewRoutePopup.findViewById<TextView>(R.id.tvSteps_detail_route_info_layout)
        val dividerAboveRecyclerView = viewRoutePopup.findViewById<LinearLayout>(R.id.recycler_view_divider_steps_layout)
        val dividerAboveReportDetail = viewRoutePopup.findViewById<LinearLayout>(R.id.report_detail_divider_route_info_layout)

        tvRouteDuration.text = route.duration!!.text
        tvRouteDistance.text = route.distance!!.text

        btnStartNavigation.setOnClickListener {
            onBtnStartNavigationClick(route)
        }

        btnSteps.setOnClickListener {
            val recyclerView = viewRoutePopup.findViewById<RecyclerView>(R.id.recycler_view_steps_layout)
            if (recyclerView.visibility == View.GONE) {
                recyclerView.visibility = View.VISIBLE
                currentStepsLayout = recyclerView
                initStepRecyclerView(route, viewRoutePopup)

                val layoutReport = viewRoutePopup.findViewById<LinearLayout>(R.id.layoutReport_detail)
                layoutReport.visibility = View.GONE
                tvBackToMap.text = "QUAY LẠI BẢN ĐỒ"
                dividerAboveRecyclerView.visibility = View.VISIBLE
            } else {
                currentStepsLayout!!.visibility = View.GONE
                currentStepsLayout = null
                if (listReportMarkerCurrentRoute.size > 0) {
                    val layoutReport = viewRoutePopup.findViewById<LinearLayout>(R.id.layoutReport_detail)
                    layoutReport.visibility = View.VISIBLE
                }
                tvBackToMap.text = "CHI TIẾT CÁC BƯỚC"
                dividerAboveRecyclerView.visibility = View.GONE
            }
        }

        // Count report on route
        val currentRoute = currentPolyline.tag as Route
        listReportMarkerCurrentRoute = ArrayList()
        listReportCurrentRoute = ArrayList()

        for (i in 0 until listReportMarker.size) {
            if (PolyUtil.isLocationOnPath(LatLng(listReportMarker[i].position.latitude, listReportMarker[i].position.longitude), currentRoute.points, true, REPORT_ON_ROUTE_DISTANCE_DIRECTION)) {
                listReportMarkerCurrentRoute.add(listReportMarker[i])
            }
        }

        listReportMarkerCurrentRoute.sortedWith(compareBy { (it.tag as Report).distance })

        for (i in 0 until listReportMarkerCurrentRoute.size) {
            listReportCurrentRoute.add(listReportMarkerCurrentRoute[i].tag as Report)
        }

        val layoutReport = viewRoutePopup.findViewById<LinearLayout>(R.id.layoutReport_detail)

        Log.v("ReportCount", "NumReport = " + listReportMarkerCurrentRoute.size.toString())

        if (listReportMarkerCurrentRoute.size > 0) {
            dividerAboveReportDetail.visibility = View.VISIBLE
            layoutReport.visibility = View.VISIBLE
            tvReportCount.visibility = View.VISIBLE
            tvReportCount.text = listReportMarkerCurrentRoute.size.toString() + " báo hiệu"

            val btnPreviousReport = viewRoutePopup.findViewById<ImageView>(R.id.btnPrevious_report_detail)
            val btnNextReport = viewRoutePopup.findViewById<ImageView>(R.id.btnNext_report_detail)
            val btnCurrentReport = viewRoutePopup.findViewById<LinearLayout>(R.id.btnCurrent_report_detail)
            val progressBar = viewRoutePopup.findViewById<ProgressBar>(R.id.progressBar_report_detail)
            progressBar.max = listReportMarkerCurrentRoute.size
            if (listReportMarkerCurrentRoute.size == 1) {
                progressBar.visibility = View.GONE
                btnNextReport.visibility = View.INVISIBLE
                btnPreviousReport.visibility = View.INVISIBLE
            } else {
                progressBar.visibility = View.VISIBLE
                btnNextReport.visibility = View.VISIBLE
                btnPreviousReport.visibility = View.VISIBLE
            }
            progressBar.progress = 1

            var currentReportIndex = 0

            updateUIReportDetail(listReportCurrentRoute[currentReportIndex], viewRoutePopup)

            btnCurrentReport.setOnClickListener {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(listReportMarkerCurrentRoute[currentReportIndex].position))
            }

            btnPreviousReport.setOnClickListener {
                //                Toast.makeText(this,"onBtnPreviousReportClick",Toast.LENGTH_SHORT).show()
                if (currentReportIndex > 0) {
                    currentReportIndex--
                    progressBar.progress--
                    Log.v("ReportCount", "currentReportIndex = " + currentReportIndex.toString())

                    updateUIReportDetail(listReportCurrentRoute[currentReportIndex], viewRoutePopup)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(listReportMarkerCurrentRoute[currentReportIndex].position))
                }
            }
            btnNextReport.setOnClickListener {
                //                Toast.makeText(this,"onBtnNextReportClick",Toast.LENGTH_SHORT).show()
                if (currentReportIndex + 1 < listReportMarkerCurrentRoute.size) {
                    currentReportIndex++
                    progressBar.progress++
                    Log.v("ReportCount", "currentReportIndex = " + currentReportIndex.toString())

                    updateUIReportDetail(listReportCurrentRoute[currentReportIndex], viewRoutePopup)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(listReportMarkerCurrentRoute[currentReportIndex].position))
                }
            }
        } else {
            layoutReport.visibility = View.GONE
            dividerAboveReportDetail.visibility = View.GONE
        }
    }

    private fun updateUIReportDetail(report: Report, view: View) {
        val imReportIcon = view.findViewById<ImageView>(R.id.imIcon_report_detail)
        val tvReportType = view.findViewById<TextView>(R.id.tvType_report_detail)
        val tvReportDistance = view.findViewById<TextView>(R.id.tvDistance_report_detail)
        val tvReportAddress = view.findViewById<TextView>(R.id.tvAddress_report_detail)

        // Làm tròn số double
        val decimalFormat = DecimalFormat("#")
        decimalFormat.roundingMode = RoundingMode.CEILING

        tvReportDistance.text = "Cách " + decimalFormat.format(report.distance) + " m"

        // Lấy địa chỉ sử dụng Geocoder
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val yourAddresses: List<Address>
            yourAddresses = geocoder.getFromLocation(report.geometry!!.coordinates!![1], report.geometry!!.coordinates!![0], 1)

            if (yourAddresses.isNotEmpty()) {
//                val yourAddress = yourAddresses.get(0).getAddressLine(0)
//                val yourCity = yourAddresses.get(0).getAddressLine(1)
//                val yourCountry = yourAddresses.get(0).getAddressLine(2)
                val address = yourAddresses.get(0).thoroughfare + ", " + yourAddresses.get(0).locality + ", " + yourAddresses.get(0).subAdminArea
                tvReportAddress.text = address
            }

        } catch (ex: Exception) {
        }

        when (report.type) {
            "traffic" -> {
                imReportIcon.background = getDrawable(R.drawable.bg_btn_report_traffic)
                when (report.subtype1) {
                    "moderate" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_traffic_moderate)
                        tvReportType.text = "Kẹt xe vừa"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.ket_xe_vua)
                        }
                    }
                    "heavy" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_traffic_heavy)
                        tvReportType.text = "Kẹt xe nặng"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.ket_xe_nang)
                        }
                    }
                    "standstill" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_traffic_standstill)
                        tvReportType.text = "Kẹt xe cứng"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.ket_xe_cung)
                        }
                    }
                }
            }
            "crash" -> {
                imReportIcon.background = getDrawable(R.drawable.bg_btn_report_crash)
                when (report.subtype1) {
                    "minor" -> {
                        imReportIcon.setImageResource(R.drawable.ic_accident_minor)
                        tvReportType.text = "Tai nạn nhỏ"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.tai_nan_nho)
                        }
                    }
                    "major" -> {
                        imReportIcon.setImageResource(R.drawable.ic_accident_major)
                        tvReportType.text = "Tai nạn nghiêm trọng"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.tai_nan_nghiem_trong)
                        }
                    }
                    "other_side" -> {
                        imReportIcon.setImageResource(R.drawable.ic_accident_other_side)
                        tvReportType.text = "Tai nạn bên đường"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.tai_nan_ben_duong)
                        }
                    }
                }
            }
            "hazard" -> {
                imReportIcon.background = getDrawable(R.drawable.bg_btn_report_hazard)
                when (report.subtype2) {
                    "object" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_hazard_object)
                        tvReportType.text = "Vật cản"
                        // Chạy audio
                        if (AppController.soundMode == 1) {
                            mAudioPlayer.play(this, R.raw.vat_can)
                        }
                    }
                    "construction" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_hazard_construction)
                        tvReportType.text = "Công trình"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.cong_trinh)
                        }
                    }
                    "broken_light" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_broken_traffic_light)
                        tvReportType.text = "Đèn báo hư"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.den_bao_hu)
                        }
                    }
                    "pothole" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_hazard_pothole)
                        tvReportType.text = "Hố voi"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.ho_voi)
                        }
                    }
                    "vehicle_stop" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_hazard_stopped)
                        if (report.subtype1 == "on_road") {
                            tvReportType.text = "Xe đậu"
                            // Chạy audio
                            if (AppController.soundMode == 1) {
                                mAudioPlayer.play(this, R.raw.xe_dau)
                            }
                        }
                        if (report.subtype1 == "shoulder") {
                            tvReportType.text = "Xe đậu bên lề"
                            // Chạy audio
                            if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                mAudioPlayer.play(this, R.raw.xe_dau_ben_le)
                            }
                        }
                    }
                    "road_kill" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_hazard_roadkill)
                        tvReportType.text = "Động vật chết"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.dong_vat_chet_tren_duong)
                        }
                    }
                    "animal" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_hazard_animals)
                        tvReportType.text = "Động vật nguy hiểm"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.dong_vat_nguy_hiem)
                        }
                    }
                    "missing_sign" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_hazard_missingsign)
                        tvReportType.text = "Thiếu biển báo"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.thieu_bien_bao)
                        }
                    }
                    "fog" -> {
                        imReportIcon.setImageResource(R.drawable.ic_hazard_weather_fog)
                        tvReportType.text = "Sương mù"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.suong_mu)
                        }
                    }
                    "hail" -> {
                        imReportIcon.setImageResource(R.drawable.ic_hazard_weather_hail)
                        tvReportType.text = "Mưa đá"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.mua_da)
                        }
                    }
                    "flood" -> {
                        imReportIcon.setImageResource(R.drawable.ic_hazard_weather_flood)
                        tvReportType.text = "Lũ lụt"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.lu_lut)
                        }
                    }
                    "ice" -> {
                        imReportIcon.setImageResource(R.drawable.ic_hazard_weather_ice)
                        tvReportType.text = "Đá trơn"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.da_tron_tren_duong)
                        }
                    }
                }
            }
            "help" -> {
                imReportIcon.background = getDrawable(R.drawable.bg_btn_report_assistance)
                when (report.subtype2) {
                    "no_gas" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_sos_no_gas)
                        tvReportType.text = "Hết xăng"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.het_xang)
                        }
                    }
                    "flat_tire" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_sos_flat_tire)
                        tvReportType.text = "Xẹp lốp xe"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.xep_lop_xe)
                        }
                    }
                    "no_battery" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_sos_no_battery)
                        tvReportType.text = "Hết bình"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.het_binh)
                        }
                    }
                    "medical_care" -> {
                        imReportIcon.setImageResource(R.drawable.ic_report_sos_medical_care)
                        tvReportType.text = "Chăm sóc y tế"
                        // Chạy audio
                        if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                            mAudioPlayer.play(this, R.raw.cham_soc_y_te)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun onBtnStartNavigationClick(route: Route) {
        dismissPopupWindowDirectionInfo()

        if (::lastLocation.isInitialized) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 20f))
            // Phải cách trong code vì nếu để cùng loại animate, và gần nhau thì 1 trong 2 cái ko kịp thực hiện làm ko thể cập nhật vị trí theo thời gian
            // moveCamera cho điểm, animateCamera cho CameraPosition
            val camPos = CameraPosition.builder()
                    .target(mMap.cameraPosition.target)
                    .zoom(20f)
                    .tilt(80f)
                    .bearing(lastLocation.bearing)
                    .build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))

//            Toast.makeText(this@MainActivity, "bearing" + lastLocation.bearing.toString(), Toast.LENGTH_SHORT).show()
        }
//            Toast.makeText(this,"onBtnStartNavigationClick",Toast.LENGTH_SHORT).show()
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewNavigationPopup = inflater.inflate(R.layout.navigation_layout, null)
        mPopupWindowNavigationInfo = PopupWindow(viewNavigationPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mPopupWindowNavigationInfo!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)
        isNavigationInfoWindowUp = true

        val imInstruction = viewNavigationPopup.findViewById<ImageView>(R.id.imInstruction_navigation_layout)
        val tvInstruction = viewNavigationPopup.findViewById<TextView>(R.id.tvInstruction_navigation_layout)
        val tvDistance = viewNavigationPopup.findViewById<TextView>(R.id.tvDistance_navigation_layout)

        // imInstruction set source
        val currentStep = getNavigationInstruction(route)
        tvInstruction.text = currentStep.instruction
        tvDistance.text = currentStep.distance!!.text
        when (currentStep.maneuver) {
            "ferry" -> {
                imInstruction.setImageResource(R.drawable.ferry)
            }
            "ferry-train" -> {
                imInstruction.setImageResource(R.drawable.ferry_train)
            }
            "fork-left" -> {
                imInstruction.setImageResource(R.drawable.fork_left)
            }
            "fork-right" -> {
                imInstruction.setImageResource(R.drawable.fork_right)
            }
            "keep-left" -> {
                imInstruction.setImageResource(R.drawable.keep_left)
            }
            "keep-right" -> {
                imInstruction.setImageResource(R.drawable.keep_right)
            }
            "merge" -> {
                imInstruction.setImageResource(R.drawable.merge)
            }
            "ramp-left" -> {
                imInstruction.setImageResource(R.drawable.ramp_left)
            }
            "ramp-right" -> {
                imInstruction.setImageResource(R.drawable.ramp_right)
            }
            "roundabout-left" -> {
                imInstruction.setImageResource(R.drawable.roundabout_left)
            }
            "roundabout-right" -> {
                imInstruction.setImageResource(R.drawable.roundabout_right)
            }
            "straight" -> {
                imInstruction.setImageResource(R.drawable.straight)
            }
            "turn-left" -> {
                imInstruction.setImageResource(R.drawable.turn_left)
            }
            "turn-right" -> {
                imInstruction.setImageResource(R.drawable.turn_right)
            }
            "turn-sharp-left" -> {
                imInstruction.setImageResource(R.drawable.turn_sharp_left)
            }
            "turn-sharp-right" -> {
                imInstruction.setImageResource(R.drawable.turn_sharp_right)
            }
            "turn-slight-left" -> {
                imInstruction.setImageResource(R.drawable.turn_slight_left)
            }
            "turn-slight-right" -> {
                imInstruction.setImageResource(R.drawable.turn_slight_right)
            }
            "uturn-left" -> {
                imInstruction.setImageResource(R.drawable.uturn_left)
            }
            "uturn-right" -> {
                imInstruction.setImageResource(R.drawable.uturn_right)
            }
        }
    }

    private fun getNavigationEndLocation(route: Route): LatLng? {
        for (iL in 0 until route.legs!!.size) {
            for (iS in 0 until route.legs!![iL].steps!!.size) {
                if (PolyUtil.isLocationOnPath(LatLng(lastLocation.latitude, lastLocation.longitude), route.legs!![iL].steps!![iS].points, true, REPORT_ON_ROUTE_DISTANCE_DIRECTION)) {
                    return route.legs!![iL].steps!![iS].endLocation
                }
            }
        }
        return route.legs!![route.legs!!.size - 1].steps!![route.legs!![route.legs!!.size - 1].steps!!.size - 1].endLocation
    }

    private var haveNotReadSecondTime = true

    private var countOutOfRoute = 0

    private fun isOutOfRoute(route: Route): Boolean {
        for (iL in 0 until route.legs!!.size) {
            for (iS in 0 until route.legs!![iL].steps!!.size) {
                if (PolyUtil.isLocationOnPath(LatLng(lastLocation.latitude, lastLocation.longitude), route.legs!![iL].steps!![iS].points, true, REPORT_ON_ROUTE_DISTANCE_DIRECTION)) {
                    return false
                }
            }
        }
        return true
    }

    private fun updateUINavigation(route: Route) {
        if (!isNavigationInfoWindowUp || !::viewNavigationPopup.isInitialized || !::lastLocation.isInitialized)
            return

        if (isOutOfRoute(route)) {
            countOutOfRoute++
        } else {
            countOutOfRoute = 0
        }
        Log.d("ReDirection", "countOutOfRoute = " + countOutOfRoute.toString())

        if (countOutOfRoute >= 3) {
            val newRoute = ArrayList<SimplePlace>()
            newRoute.add(SimplePlace("Vị trí của bạn", LatLng(lastLocation.latitude, lastLocation.longitude)))
            for (i in 1 until currentDirectionRoute.size) {
                newRoute.add(currentDirectionRoute[i])
            }
            currentDirectionRoute.clear()
            currentDirectionRoute = newRoute

            onBtnStartDirectionClick(currentDirectionRoute)

            val currentChosenRoute = currentPolyline.tag as Route?
            dismissPopupWindowNavigationInfo()
            if (currentChosenRoute != null) {
                onBtnStartNavigationClick(currentChosenRoute)
            } else {
                Log.d("ReDirection", "currentChosenRoute = null")
                onBtnStartNavigationClick(route)
            }
            Log.d("ReDirection", "ReDirection")
            countOutOfRoute = 0
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 20f))
        // Phải cách trong code vì nếu để cùng loại animate, và gần nhau thì 1 trong 2 cái ko kịp thực hiện làm ko thể cập nhật vị trí theo thời gian
        // moveCamera cho điểm, animateCamera cho CameraPosition
        val camPos = CameraPosition.builder()
                .target(mMap.cameraPosition.target)
                .zoom(20f)
                .tilt(80f)
                .bearing(lastLocation.bearing)
                .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))


        val imInstruction = viewNavigationPopup.findViewById<ImageView>(R.id.imInstruction_navigation_layout)
        val tvInstruction = viewNavigationPopup.findViewById<TextView>(R.id.tvInstruction_navigation_layout)
        val tvDistance = viewNavigationPopup.findViewById<TextView>(R.id.tvDistance_navigation_layout)

        val currentStep = getNavigationInstruction(route)

        if (!::oldStep.isInitialized) {
            // NÓI
            haveNotReadSecondTime = true
            oldStep = currentStep
        } else {
            if (currentStep != oldStep) {
                // NÓI
                haveNotReadSecondTime = true
                oldStep = currentStep
            } else {
                val endLocation = getNavigationEndLocation(route)
                val results = FloatArray(3)
                if (endLocation != null) {
                    Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, endLocation.latitude, endLocation.longitude, results)
                }
                if (results[0] <= 100 && haveNotReadSecondTime) {
                    // NÓI
                    haveNotReadSecondTime = false
                }
            }

        }

        tvInstruction.text = currentStep.instruction
        tvDistance.text = currentStep.distance!!.text
        when (currentStep.maneuver) {
            "ferry" -> {
                imInstruction.setImageResource(R.drawable.ferry)
            }
            "ferry-train" -> {
                imInstruction.setImageResource(R.drawable.ferry_train)
            }
            "fork-left" -> {
                imInstruction.setImageResource(R.drawable.fork_left)
            }
            "fork-right" -> {
                imInstruction.setImageResource(R.drawable.fork_right)
            }
            "keep-left" -> {
                imInstruction.setImageResource(R.drawable.keep_left)
            }
            "keep-right" -> {
                imInstruction.setImageResource(R.drawable.keep_right)
            }
            "merge" -> {
                imInstruction.setImageResource(R.drawable.merge)
            }
            "ramp-left" -> {
                imInstruction.setImageResource(R.drawable.ramp_left)
            }
            "ramp-right" -> {
                imInstruction.setImageResource(R.drawable.ramp_right)
            }
            "roundabout-left" -> {
                imInstruction.setImageResource(R.drawable.roundabout_left)
            }
            "roundabout-right" -> {
                imInstruction.setImageResource(R.drawable.roundabout_right)
            }
            "straight" -> {
                imInstruction.setImageResource(R.drawable.straight)
            }
            "turn-left" -> {
                imInstruction.setImageResource(R.drawable.turn_left)
            }
            "turn-right" -> {
                imInstruction.setImageResource(R.drawable.turn_right)
            }
            "turn-sharp-left" -> {
                imInstruction.setImageResource(R.drawable.turn_sharp_left)
            }
            "turn-sharp-right" -> {
                imInstruction.setImageResource(R.drawable.turn_sharp_right)
            }
            "turn-slight-left" -> {
                imInstruction.setImageResource(R.drawable.turn_slight_left)
            }
            "turn-slight-right" -> {
                imInstruction.setImageResource(R.drawable.turn_slight_right)
            }
            "uturn-left" -> {
                imInstruction.setImageResource(R.drawable.uturn_left)
            }
            "uturn-right" -> {
                imInstruction.setImageResource(R.drawable.uturn_right)
            }
        }
    }

    private fun getNavigationInstruction(route: Route): Step {
        for (iL in 0 until route.legs!!.size) {
            for (iS in 0 until route.legs!![iL].steps!!.size) {
//                val line = ArrayList<LatLng>()

                val options = PolylineOptions()
                options.color(Color.RED)
                options.width(5f)
                options.zIndex(2F)

                for (iP in 0 until route.legs!![iL].steps!![iS].points!!.size) {
//                    line.add(route.legs!![iL].steps!![iS].points!![iP])

                    options.add(route.legs!![iL].steps!![iS].points!![iP])
                }

//                val tmpLine = mMap.addPolyline(options)

                if (PolyUtil.isLocationOnPath(LatLng(lastLocation.latitude, lastLocation.longitude), route.legs!![iL].steps!![iS].points, true, REPORT_ON_ROUTE_DISTANCE_DIRECTION)) {
                    // The polyline is composed of great circle segments if geodesic is true, and of Rhumb segments otherwise
                    Log.v("Navigation", "OnPathOK")
                    return if (iS + 1 < route.legs!![iL].steps!!.size) {
                        route.legs!![iL].steps!![iS + 1]
                    } else {
                        if (iL + 1 < route.legs!!.size) {
                            route.legs!![iL + 1].steps!![0]
                        } else {
                            route.legs!![iL].steps!![iS]
                        }
                    }
                }
//                tmpLine.remove()
            }
        }
        Log.v("Navigation", "OnPathFALSE")
        return route.legs!![0].steps!![0]
    }

    private fun dismissPopupWindowRouteInfo() {
        mPopupWindowRouteInfo?.dismiss()
        isRouteInfoWindowUp = false
        if (mPopupWindowPlaceInfo != null && !polylinePaths.isNotEmpty()) {
            mPopupWindowPlaceInfo!!.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)
            isPlaceInfoWindowUp = true
        }
    }

    private fun dismissPopupWindowNavigationInfo() {
        mPopupWindowNavigationInfo?.dismiss()
        isNavigationInfoWindowUp = false
        if (mPopupWindowDirectionInfo != null) {
            mPopupWindowDirectionInfo!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)
            isDirectionInfoWindowUp = true
        }
    }

    private fun dismissPopupWindowDirectionInfo() {
        mPopupWindowDirectionInfo?.dismiss()
        isDirectionInfoWindowUp = false
    }

    private fun dismissPopupWindowEditDirection() {
        mPopupWindowEditDirection?.dismiss()
        isEditDirectionWindowUp = false
//        if (mPopupWindowDirectionInfo!=null){
//
//        }
    }

    // ========================================================================================================================================= //
    // ======== VỀ MAIN ======================================================================================================================== //
    // ========================================================================================================================================= //
    private lateinit var placeAutoComplete: PlaceAutocompleteFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // Obtain placeAutoComplete fragment
        // PlaceAutoCompleteFragment
        placeAutoComplete = fragmentManager.findFragmentById(R.id.place_autocomplete)
                as PlaceAutocompleteFragment

        val typeFilter = AutocompleteFilter.Builder().setCountry("VN").build()
        placeAutoComplete.setFilter(typeFilter)
        placeAutoComplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Maps", "Place selected: " + place.name)
                dismissPopupWindowPlaceInfo()
                removeCurrentSelectedPlace()
                currentSelectedPlace = place

                addMarker(place)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng,17f))

                showPlaceInfoPopup(place)

                // Thêm place vào AppController
                if (AppController.listHistoryPlace.size >= 3) {
                    for (i in 0 until 2) {
                        if (i == 2) {
                            AppController.listHistoryPlace[i] = place
                        } else {
                            val temp = i + 1
                            AppController.listHistoryPlace[i] = AppController.listHistoryPlace[temp]
                        }
                    }
                } else {
                    AppController.listHistoryPlace.add(place)
                }

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
        currentSocketSetting = AppController.settingSocket
        if (currentSocketSetting == "true") {
            initSocket()
        }

        // onClickListener cho các nút
        imvMyLoc.setOnClickListener(this)
        imvReport.setOnClickListener(this)
//        imvHamburger.setOnClickListener(this)

        imvFilter.setOnClickListener(this)


        // Khởi tạo các nút trên menu drawer
        initMenuItemDrawer()

        mContext = this.applicationContext

        // Chạy audio
        if (AppController.soundMode == 1) {
            mAudioPlayer.play(this, R.raw.chao_ban_den_voi_car_map)
        }
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
                imQuickSettingSound.setImageResource(R.drawable.ic_sound_on)
                tvQuickSettingSound.text = "MỞ"
            }
            2 -> {
                imQuickSettingSound.setImageResource(R.drawable.ic_sound_alerts)
                tvQuickSettingSound.text = "CHỈ CÁC BÁO HIỆU"
            }
            3 -> {
                imQuickSettingSound.setImageResource(R.drawable.ic_sound_mute)
                tvQuickSettingSound.text = "TẮT"
            }
        }

        layoutSignOut.setOnClickListener(this)

        layoutEditHome.setOnClickListener(this)

        layoutEditWork.setOnClickListener(this)
    }

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
        if (isTouchSoundsEnabled) {
            Settings.System.putInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 0)
        }
        if (isTouchVibrateEnabled) {
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
        if (::fusedLocationClient.isInitialized) {
            startLocationUpdates()
        }
//        // Chạy audio
//        if (AppController.soundMode == 1) {
////            try {
////                val mp = MediaPlayer.create(this, R.raw.chao_ban_den_voi_car_map)
////                mp.prepare()
////                mp.start()
////            } catch (e: Exception) {
////                e.printStackTrace()
////            }
//            mAudioPlayer.play(this, R.raw.chao_ban_den_voi_car_map)
//        }

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
                    val user = User("", "", "", "", "", "", "", newGeo, 0.0, 0.0, 0.0, 0.0, "", "", "", "", "")
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
        handler.postDelayed(runnable, 3000)  //the time is in miliseconds

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
            // Khởi tạo sound và vibrate
            initSoundVibrate()
        } else {
            // Migrate to Setting write permission screen
            val intent: Intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.packageName)
            startActivity(intent)
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
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this, R.raw.vi_tri_hien_tai)
                }
                onMyLocationButtonClicked()

//                val customTarget = CustomTarget.Builder(this)
//                        .setPoint(100f, 340f)
//                        .setShape(Circle(200f))
//                        .setOverlay(v)
//                        .setOnSpotlightStartedListener(object : OnTargetStateChangedListener<CustomTarget> {
//                            override fun onStarted(target: CustomTarget) {
//
//                            }
//
//                            override fun onEnded(target: CustomTarget?) {
//
//                            }
//                        })
//                        .build()
//                Spotlight.with(this)
//                        .setOverlayColor(R.color.background_front)
//                        .setDuration(1000L)
//                        .setAnimation(DecelerateInterpolator(2f))
//                        .setTargets(customTarget)
//                        .setClosedOnTouchedOutside(false)
//                        .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
//                            override fun onStarted() {
//                                Toast.makeText(this@MainActivity, "spotlight is started", Toast.LENGTH_SHORT).show();
//                            }
//
//                            override fun onEnded() {
//                                Toast.makeText(this@MainActivity, "spotlight is ended", Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .start()
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
                if (AppController.userProfile != null) {
                    if (AppController.userProfile!!.latHomeLocation != null && AppController.userProfile!!.longHomeLocation != null) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                        currentDirectionRoute.clear()
                        currentDirectionRoute.add(SimplePlace("Vị trí của bạn", LatLng(lastLocation.latitude, lastLocation.longitude)))
                        currentDirectionRoute.add(SimplePlace("Nhà", LatLng(AppController.userProfile!!.latHomeLocation!!, AppController.userProfile!!.longHomeLocation!!)))
                        onBtnStartDirectionClick(currentDirectionRoute)
                    } else {
                        Log.v("Direction", "User Home not set")
                    }
                } else {
                    Log.v("Direction", "User Profile not found")
                }
            }
            R.id.layoutWorkMenu -> {
                if (AppController.userProfile != null) {
                    if (AppController.userProfile!!.latWorkLocation != null && AppController.userProfile!!.longWorkLocation != null) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                        currentDirectionRoute.clear()
                        currentDirectionRoute.add(SimplePlace("Vị trí của bạn", LatLng(lastLocation.latitude, lastLocation.longitude)))
                        currentDirectionRoute.add(SimplePlace("Nhà", LatLng(AppController.userProfile!!.latWorkLocation!!, AppController.userProfile!!.longWorkLocation!!)))
                        onBtnStartDirectionClick(currentDirectionRoute)
                    } else {
                        Log.v("Direction", "User Work not set")
                    }
                } else {
                    Log.v("Direction", "User Profile not found")
                }
            }
            R.id.layoutHistoryMenu -> {
                val intent = Intent(this, HistorySettingActivity::class.java)
                startActivityForResult(intent, PICK_PLACE_HISTORY_REQUEST)
            }
            R.id.layoutSettingMenu -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivityForResult(intent, 3)
            }
            R.id.layoutQuickSettingSound -> {
                when (AppController.soundMode) {
                    1 -> {
                        // Chạy audio
                        if (AppController.soundMode == 1) {
                            mAudioPlayer.play(this, R.raw.am_thanh_chi_bao_hieu)
                        }
                        AppController.soundMode = 2
                        imQuickSettingSound.setImageResource(R.drawable.ic_sound_alerts)
                        tvQuickSettingSound.text = "CHỈ CÁC BÁO HIỆU"
                    }
                    2 -> {
                        // Chạy audio
                        if (AppController.soundMode == 1) {
                            mAudioPlayer.play(this, R.raw.am_thanh_tat)
                        }
                        AppController.soundMode = 3
                        imQuickSettingSound.setImageResource(R.drawable.ic_sound_mute)
                        tvQuickSettingSound.text = "TẮT"
                    }
                    3 -> {
                        // Chạy audio
                        if (AppController.soundMode == 1) {
                            mAudioPlayer.play(this, R.raw.am_thanh_mo)
                        }
                        AppController.soundMode = 1
                        imQuickSettingSound.setImageResource(R.drawable.ic_sound_on)
                        tvQuickSettingSound.text = "MỞ"
                    }
                }

            }
            R.id.layoutSignOut -> {
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this, R.raw.dang_xuat)
                }
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
                    val user = User("", "", "", "", "", "", "", newGeo, AppController.userProfile!!.latHomeLocation!!, AppController.userProfile!!.longHomeLocation!!, 0.0, 0.0, "", "", "", "", "")
                    onUpdateHomeLocation(user)
                }
            }
            2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    tvAddressWork_menu.text = data!!.getStringExtra("work_location_new")
                    val listGeo: List<Double> = listOf(0.0, 0.0)
                    val newGeo = Geometry("Point", listGeo)
                    val user = User("", "", "", "", "", "", "", newGeo, 0.0, 0.0, AppController.userProfile!!.latWorkLocation!!, AppController.userProfile!!.longWorkLocation!!, "", "", "", "", "")
                    onUpdateWorkLocation(user)
                }
            }
            3 -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Set lần đầu cho setting âm thanh
                    when (AppController.soundMode) {
                        1 -> {
                            imQuickSettingSound.setImageResource(R.drawable.ic_sound_on)
                            tvQuickSettingSound.text = "MỞ"
                        }
                        2 -> {
                            imQuickSettingSound.setImageResource(R.drawable.ic_sound_alerts)
                            tvQuickSettingSound.text = "CHỈ CÁC BÁO HIỆU"
                        }
                        3 -> {
                            imQuickSettingSound.setImageResource(R.drawable.ic_sound_mute)
                            tvQuickSettingSound.text = "TẮT"
                        }
                    }

                    if (AppController.settingSocket == "true" && AppController.settingSocket != currentSocketSetting) {
                        initSocket()
                        currentSocketSetting = AppController.settingSocket
                    }

                    if (AppController.settingSocket == "false" && AppController.settingSocket != currentSocketSetting) {
                        destroySocket()
                        currentSocketSetting = AppController.settingSocket
                    }

//                    if (AppController.settingInvisible == "invisible" && AppController.settingInvisible != currentStatusSetting) {
                    if (AppController.settingInvisible == "invisible") {
                        val listGeo: List<Double> = listOf(0.0, 0.0)
                        val newGeo = Geometry("Point", listGeo)
                        val user = User("", "", "", "", "", "", "", newGeo, 0.0, 0.0, 0.0, 0.0, "", "", "", "invisible", "")
                        onUpdateStatus(user)
//                        currentStatusSetting = AppController.settingInvisible
                    }

//                    if (AppController.settingInvisible == "visible" && AppController.settingInvisible != currentStatusSetting) {
                    if (AppController.settingInvisible == "visible") {
                        val listGeo: List<Double> = listOf(0.0, 0.0)
                        val newGeo = Geometry("Point", listGeo)
                        val user = User("", "", "", "", "", "", "", newGeo, 0.0, 0.0, 0.0, 0.0, "", "", "", "visible", "")
                        onUpdateStatus(user)
//                        currentStatusSetting = AppController.settingInvisible
                    }

                    if (AppController.userProfile!!.typeCar != "" || AppController.userProfile!!.modelCar != "" || AppController.userProfile!!.colorCar != "") {
                        val listGeo: List<Double> = listOf(0.0, 0.0)
                        val newGeo = Geometry("Point", listGeo)
                        val user = User("", "", "", "", "", "", "", newGeo, 0.0, 0.0, 0.0, 0.0, AppController.userProfile!!.typeCar.toString(), AppController.userProfile!!.modelCar.toString(), AppController.userProfile!!.colorCar.toString(), "", "")
                        onUpdateMyCar(user)
                    }
                }
            }
            PICK_PLACE_HISTORY_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = SimplePlace(data.getStringExtra("PLACE_NAME"),
                            LatLng(data.getDoubleExtra("PLACE_LAT", 0.0), data.getDoubleExtra("PLACE_LONG", 0.0)))
                    currentDirectionRoute.clear()
                    currentDirectionRoute.add(SimplePlace("Vị trí của bạn", LatLng(lastLocation.latitude, lastLocation.longitude)))
                    currentDirectionRoute.add(SimplePlace(place.name, LatLng(place.location!!.latitude, place.location!!.longitude)))
                    drawer_layout.closeDrawer(GravityCompat.START)
                    onBtnStartDirectionClick(currentDirectionRoute)
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
//                        if (isNavigationInfoWindowUp) {
//                            Toast.makeText(this@MainActivity, "bearing" + lastLocation.bearing.toString(), Toast.LENGTH_SHORT).show()
//                            val camPos = CameraPosition.builder(mMap.cameraPosition)
////                                    .target(mMap.cameraPosition.target)
//                                    .zoom(20f)
//                                    .tilt(65.5f)
//                                    .bearing(location.bearing)
//                                    .build()
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 20f))
//                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
//                        }
                    }
                }
                // Update Navigation UI
                Log.v("Navigation", "Success Location")
                if (isNavigationInfoWindowUp) {
                    Log.v("Navigation", "Update Navigation UI")
//                    mPopupWindowNavigationInfo?.dismiss()
                    val currentRoute = currentPolyline.tag as Route
//                    onBtnStartNavigationClick(currentRoute)
                    updateUINavigation(currentRoute)

                    // onReachReportMarker
                    listReportMarker.sortedWith(compareBy { (it.tag as Report).distance })
                    mPopupWindowReport?.dismiss()
                    for (i in 0 until listReportMarker.size) {
                        val location = Location("tempLocation")
                        location.latitude = listReportMarker[i].position.latitude
                        location.longitude = listReportMarker[i].position.longitude

                        // Khoảng cách 20m thì hiện báo hiệu
                        if (lastLocation.distanceTo(location) < REPORT_ON_ROUTE_DISTANCE_NAVIGATION) {
                            onOpenReportMarker(listReportMarker[i])
                        }
                        break
                    }
                }
            }
        }

        // Set up a location request
        createLocationRequest()
    }

    private lateinit var locationTask: Task<LocationSettingsResponse>

    @SuppressLint("RestrictedApi")
    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 10000
            // change to 2000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)

        locationTask = client.checkLocationSettings(builder.build())

        locationTask.addOnSuccessListener {
            startLocationUpdates()
        }

        locationTask.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                val lm: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                var needToEnableLocation = false
                try {
                    val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    if (!isGpsEnabled && !isNetworkEnabled) {
                        needToEnableLocation = true
                    }
                } catch (ex: Exception) {
                }


                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    if (needToEnableLocation) {
                        e.startResolutionForResult(this@MainActivity,
                                REQUEST_CHECK_SETTINGS)
                    }
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

//    private fun updateCameraBearing(googleMap: GoogleMap, bearing: Float) {
//        if (googleMap == null) return
////        val camPos = CameraPosition
////                .builder(
////                        googleMap.cameraPosition // current Camera
////                )
////                .bearing(bearing)
////                .build()
//        val camPos = CameraPosition(googleMap.cameraPosition.target, 20f, 65.5f, bearing)
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
//    }

    // ================================================================================================================================================== //
    // ======== VỀ CÁC NÚT TRÊN APP BAR MAIN ============================================================================================================ //
    // ================================================================================================================================================== //

    @SuppressLint("MissingPermission")
    private fun onMyLocationButtonClicked() {
        mLocationPermission.execute()
        if (::locationTask.isInitialized) {
            locationTask.addOnFailureListener { e ->
                // 6
                if (e is ResolvableApiException) {
                    val lm: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    var needToEnableLocation = false
                    try {
                        val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        if (!isGpsEnabled && !isNetworkEnabled) {
                            needToEnableLocation = true
                        }
                    } catch (ex: Exception) {
                    }
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        if (needToEnableLocation) {
                            e.startResolutionForResult(this@MainActivity,
                                    REQUEST_CHECK_SETTINGS)
                        }
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        }
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) lastLocation = location
            }
        }
        if (::lastLocation.isInitialized) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 17f))
        } else {
//            TastyToast.makeText(this, "Vị trí hiện không khả dụng!", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
        }
    }

    @SuppressLint("InflateParams")
    private fun onFilterButtonClicked() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewFilterPopup = inflater.inflate(R.layout.filter_dialog_layout, null)
        // Dùng với layout cũ
//        mPopupWindowFilter = PopupWindow(viewFilterPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        mPopupWindowFilter!!.showAtLocation(this.currentFocus, Gravity.NO_GRAVITY, (imvFilter.x.toInt() / 2) - (imvFilter.x.toInt() / 6), imvFilter.y.toInt())

        // Layout mới
        mPopupWindowFilter = PopupWindow(viewFilterPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mPopupWindowFilter!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)
        val btnClose = viewFilterPopup.findViewById<ImageView>(R.id.imClose_filter_dialog)
        val switchCar = viewFilterPopup.findViewById<LabeledSwitch>(R.id.switchFilterCar_filter_dialog)
        val switchReport = viewFilterPopup.findViewById<LabeledSwitch>(R.id.switchFilterReport_filter_dialog)
        val layoutOutside = viewFilterPopup.findViewById<LinearLayout>(R.id.bg_to_remove_filter_dialog)

        switchCar.isOn = AppController.settingFilterCar == "true"

        switchReport.isOn = AppController.settingFilterReport == "true"


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

        switchCar.setOnToggledListener { _, isOn ->
            if (isOn) {
//                    Toast.makeText(this@MainActivity, "Car on", Toast.LENGTH_SHORT).show()
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this@MainActivity, R.raw.hien_tai_xe)
                }
                AppController.settingFilterCar = "true"
            } else {
//                    Toast.makeText(this@MainActivity, "Car off", Toast.LENGTH_SHORT).show()
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this@MainActivity, R.raw.an_tai_xe)
                }
                AppController.settingFilterCar = "false"
            }
        }

        switchReport.setOnToggledListener { _, isOn ->
            if (isOn) {
//                    Toast.makeText(this@MainActivity, "Report on", Toast.LENGTH_SHORT).show()
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this@MainActivity, R.raw.hien_bao_hieu)
                }
                AppController.settingFilterReport = "true"
            } else {
//                    Toast.makeText(this@MainActivity, "Report off", Toast.LENGTH_SHORT).show()
                // Chạy audio
                if (AppController.soundMode == 1) {
                    mAudioPlayer.play(this@MainActivity, R.raw.an_bao_hieu)
                }
                AppController.settingFilterReport = "false"
            }
        }

        layoutOutside.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            mPopupWindowFilter!!.dismiss()
        }
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> {
                drawer_layout.closeDrawer(GravityCompat.START)
                return
            }
            isAddPlaceWindowUp -> {
                dismissAddPlacePopup()
                return
            }
            currentStepsLayout != null -> {
                currentStepsLayout!!.visibility = View.GONE
                currentStepsLayout = null
                if (listReportMarkerCurrentRoute.size > 0) {
                    val layoutReport = viewRoutePopup.findViewById<LinearLayout>(R.id.layoutReport_detail)
                    layoutReport.visibility = View.VISIBLE
                }
                return
            }
            isEditDirectionWindowUp -> {
                onFinishEditDirection()
                return
            }
            isNavigationInfoWindowUp -> {
                dismissPopupWindowNavigationInfo()

                // Đóng popup windows
                mPopupWindowReport?.dismiss()
                curMarkerReport = null

                // Update Camera
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 14f))
                val camPos = CameraPosition.builder()
                        .target(LatLng(lastLocation.latitude, lastLocation.longitude))
                        .zoom(14f)
                        .tilt(0f)
                        .build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))

                return
            }
            ::polylinePaths.isInitialized && polylinePaths.isNotEmpty() && isRouteInfoWindowUp && isDirectionInfoWindowUp -> {
                removeCurrentDirectionPolyline()
                dismissPopupWindowRouteInfo()
                dismissPopupWindowDirectionInfo()
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
            else -> {
                mPopupWindowDirectionInfo = null
                dismissPopupWindowNavigationInfo()
                val builder = android.support.v7.app.AlertDialog.Builder(this)
                builder.setMessage("Bạn có muốn thoát khỏi ứng dụng?")
                        .setCancelable(false)
                        .setPositiveButton("Có") { _, _ -> finish() }
                        .setNegativeButton("Không") { dialog, _ -> dialog.cancel() }
                val alert = builder.create()
                alert.show()
            }
//            else -> super.onBackPressed()
        }
    }

    private fun dismissPopupWindowPlaceInfo() {
        mPopupWindowPlaceInfo?.dismiss()
        isPlaceInfoWindowUp = false
        imvReport.visibility = View.VISIBLE
    }

    private fun removeCurrentSelectedPlace() {
        currentSelectedPlaceMarker?.remove()
        placeAutoComplete.setText(null)
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
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(p.latLng))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(17f))

        currentSelectedPlaceMarker = mMap.addMarker(markerOptions)
        currentSelectedPlaceMarker?.title = "current_place"
    }

    // ================================================================================================================================================= //
    // ======== VỀ THÔNG TIN NGƯỜI DÙNG USER =========================================================================================================== //
    // ================================================================================================================================================= //
    private fun loadUserProfile() {
        if (AppController.accessToken != null && AppController.accessToken.toString().isNotEmpty()) {
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
            try {
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
            } catch (ex: Exception) {
                TastyToast.makeText(this, "Kết nối mạng yếu", TastyToast.LENGTH_SHORT, TastyToast.CONFUSING).show()
            }
        }

        if (user.latWorkLocation != null && user.longWorkLocation != null) {
            try {
                // Lấy địa chỉ nhà sử dụng Geocoder
                val geocoder = Geocoder(this, Locale.getDefault())
                val yourAddresses: List<Address>
                yourAddresses = geocoder.getFromLocation(user.latWorkLocation!!, user.longWorkLocation!!, 1)

                if (yourAddresses.isNotEmpty()) {
                    val address = yourAddresses[0].thoroughfare + ", " + yourAddresses[0].locality + ", " + yourAddresses[0].subAdminArea + ", " + yourAddresses.get(0).adminArea + ", " + yourAddresses.get(0).countryName
                    tvAddressWork_menu.text = address
                }
            } catch (ex: Exception) {
                TastyToast.makeText(this, "Kết nối mạng yếu", TastyToast.LENGTH_SHORT, TastyToast.CONFUSING).show()
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
                return false
            }
        }

        if (p0.title == "nearby_places_result") {
            val place = p0.tag as SimplePlace
            currentDirectionRoute.add(place)
            viewAdapterEditDirection.notifyDataSetChanged()
            return false
        }

        onOpenReportMarker(p0)
        return false
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun onOpenReportMarker(marker: Marker) {
        if (marker.title == "report") {
            val dataReport: Report = marker.tag as Report
            if (dataReport.type == "help" || dataReport.type == "other") {
                if (dataReport.type == "help") {
                    val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val viewReportPopup = inflater.inflate(R.layout.marker_report_layout_help, null)
                    mPopupWindowReport = PopupWindow(viewReportPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    mPopupWindowReport!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)

                    // Phải có con trỏ vào customViewPopup, nếu không sẽ null
                    val tvType = viewReportPopup.findViewById<TextView>(R.id.tvType_marker_report_help)
                    val tvDistance = viewReportPopup.findViewById<TextView>(R.id.tvDistance_marker_report_help)
                    val tvLocation = viewReportPopup.findViewById<TextView>(R.id.tvLocation_marker_report_help)
                    val tvDescription = viewReportPopup.findViewById<TextView>(R.id.tvDescription_marker_report_help)
                    val imvType = viewReportPopup.findViewById<ImageView>(R.id.imvType_marker_report_help)
                    val imvUpVote = viewReportPopup.findViewById<LinearLayout>(R.id.imvUpVote_marker_report_help)
                    val imvDownVote = viewReportPopup.findViewById<ImageView>(R.id.imvDownVote_marker_report_help)
                    val imvRecord = viewReportPopup.findViewById<ImageView>(R.id.imRecord_marker_report_help)
                    val imvImage = viewReportPopup.findViewById<ImageView>(R.id.imImage_marker_report_help)
                    val imvCall = viewReportPopup.findViewById<ImageView>(R.id.imCall_marker_report_help)
                    val tvNumReport = viewReportPopup.findViewById<TextView>(R.id.tvNumReport_marker_report_help)

                    // Làm tròn số double
                    val decimalFormat = DecimalFormat("#")
                    decimalFormat.roundingMode = RoundingMode.CEILING

                    tvDistance.text = "Cách " + decimalFormat.format(dataReport.distance) + " m"

                    // Lấy địa chỉ sử dụng Geocoder
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val yourAddresses: List<Address>
                        yourAddresses = geocoder.getFromLocation(dataReport.geometry!!.coordinates!![1], dataReport.geometry!!.coordinates!![0], 1)

                        if (yourAddresses.isNotEmpty()) {
                            val address = yourAddresses.get(0).thoroughfare + ", " + yourAddresses.get(0).locality + ", " + yourAddresses.get(0).subAdminArea
                            tvLocation.text = address
                        }

                    } catch (ex: Exception) {
                    }


                    tvDescription.text = dataReport.description.toString()
                    when (dataReport.type) {
                        "help" -> {
                            imvType.background = getDrawable(R.drawable.bg_btn_report_assistance)
                            when (dataReport.subtype2) {
                                "no_gas" -> {
                                    imvType.setImageResource(R.drawable.ic_report_sos_no_gas)
                                    tvType.text = "Hết xăng"
                                    // Chạy audio
                                    if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                        mAudioPlayer.play(this, R.raw.het_xang)
                                    }
                                }
                                "flat_tire" -> {
                                    imvType.setImageResource(R.drawable.ic_report_sos_flat_tire)
                                    tvType.text = "Xẹp lốp xe"
                                    // Chạy audio
                                    if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                        mAudioPlayer.play(this, R.raw.xep_lop_xe)
                                    }
                                }
                                "no_battery" -> {
                                    imvType.setImageResource(R.drawable.ic_report_sos_no_battery)
                                    tvType.text = "Hết bình"
                                    // Chạy audio
                                    if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                        mAudioPlayer.play(this, R.raw.het_binh)
                                    }
                                }
                                "medical_care" -> {
                                    imvType.setImageResource(R.drawable.ic_report_sos_medical_care)
                                    tvType.text = "Chăm sóc y tế"
                                    // Chạy audio
                                    if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                        mAudioPlayer.play(this, R.raw.cham_soc_y_te)
                                    }
                                }
                            }
                        }
                    }

                    curMarkerReport = marker

                    // Số report
                    tvNumReport.text = dataReport.numReport.toString()

                    imvUpVote.setOnClickListener {
                        viewReportPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onUpdateNumReport(dataReport._id.toString())
                        tvNumReport.text = (dataReport.numReport!!.toInt() + 1).toString()
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
//                    Toast.makeText(this, "Remove Report", Toast.LENGTH_SHORT).show()

                            // Chạy audio
                            if (AppController.soundMode == 1) {
                                mAudioPlayer.play(this, R.raw.xoa_bao_hieu)
                            }

                            mPopupWindowReport!!.dismiss()
                            curMarkerReport = null

                            val inflater2 = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val viewDeletePopup = inflater2.inflate(R.layout.confirm_delete_report_dialog_layout, null)
                            mPopupWindowDelete = PopupWindow(viewDeletePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            mPopupWindowDelete!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)
                            val btnYes = viewDeletePopup.findViewById<Button>(R.id.btnYes_confirm_delete_report)
                            val btnNo = viewDeletePopup.findViewById<Button>(R.id.btnNo_confirm_delete_report)
                            val layoutOutside = viewDeletePopup.findViewById<LinearLayout>(R.id.bg_to_remove_confirm_delete_report)
                            btnYes.setOnClickListener {
                                viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                mPopupWindowDelete!!.dismiss()
                                onDeleteReport(dataReport._id.toString())
                            }

                            btnNo.setOnClickListener {
                                viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                mPopupWindowDelete!!.dismiss()
                            }

                            layoutOutside.setOnClickListener {
                                viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                mPopupWindowDelete!!.dismiss()
                            }
                        } else {
//                    Toast.makeText(this, "Down Vote", Toast.LENGTH_SHORT).show()
                            onUpdateNumDelete(dataReport._id.toString())
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
                    imvCall.setOnClickListener {
                        if (dataReport.phoneNumber == AppController.userProfile!!.phoneNumber) {
                            TastyToast.makeText(this, "Không thể gọi vì đây là báo hiệu của bạn", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                        } else {
                            val callURI = "tel:" + dataReport.phoneNumber
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.setData(Uri.parse(callURI))
                            startActivity(intent)
                        }
                    }
                } else {
                    val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val viewReportPopup = inflater.inflate(R.layout.marker_report_layout_other, null)
                    mPopupWindowReport = PopupWindow(viewReportPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    mPopupWindowReport!!.showAtLocation(this.currentFocus, Gravity.TOP, 0, 0)

                    // Phải có con trỏ vào customViewPopup, nếu không sẽ null
                    val tvType = viewReportPopup.findViewById<TextView>(R.id.tvType_marker_report_other)
                    val tvDistance = viewReportPopup.findViewById<TextView>(R.id.tvDistance_marker_report_other)
                    val tvLocation = viewReportPopup.findViewById<TextView>(R.id.tvLocation_marker_report_other)
                    val tvDescription = viewReportPopup.findViewById<TextView>(R.id.tvDescription_marker_report_other)
                    val imvType = viewReportPopup.findViewById<ImageView>(R.id.imvType_marker_report_other)
                    val imvUpVote = viewReportPopup.findViewById<LinearLayout>(R.id.imvUpVote_marker_report_other)
                    val imvDownVote = viewReportPopup.findViewById<ImageView>(R.id.imvDownVote_marker_report_other)
                    val imvImage = viewReportPopup.findViewById<ImageView>(R.id.imImage_marker_report_other)
                    val tvNumReport = viewReportPopup.findViewById<TextView>(R.id.tvNumReport_marker_report_other)

                    // Làm tròn số double
                    val decimalFormat = DecimalFormat("#")
                    decimalFormat.roundingMode = RoundingMode.CEILING

                    tvDistance.text = "Cách " + decimalFormat.format(dataReport.distance) + " m"

                    // Lấy địa chỉ sử dụng Geocoder
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val yourAddresses: List<Address>
                        yourAddresses = geocoder.getFromLocation(dataReport.geometry!!.coordinates!![1], dataReport.geometry!!.coordinates!![0], 1)

                        if (yourAddresses.isNotEmpty()) {
                            val address = yourAddresses.get(0).thoroughfare + ", " + yourAddresses.get(0).locality + ", " + yourAddresses.get(0).subAdminArea
                            tvLocation.text = address
                        }

                    } catch (ex: Exception) {
                    }


                    tvDescription.text = "Biển số: " + dataReport.description.toString()
                    when (dataReport.subtype1) {
                        "careless_driver" -> {
                            imvType.background = getDrawable(R.drawable.bg_btn_report_other_careless_driver)
                            imvType.setImageResource(R.drawable.ic_report_other_careless_driver)
                            tvType.text = "Tài xế chạy ẩu"
                            // Chạy audio
                            if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                mAudioPlayer.play(this, R.raw.canh_bao_co_tai_xe_chay_au)
                            }
                        }
                        "piggy" -> {
                            imvType.background = getDrawable(R.drawable.bg_btn_report_traffic)
                            imvType.setImageResource(R.drawable.ic_dangerous_zone)
                            tvType.text = "Nguy hiểm khác"
                            // Chạy audio
                            if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                mAudioPlayer.play(this, R.raw.canh_bao_co_bo_cau)
                            }
                        }
                    }

                    curMarkerReport = marker

                    // Số report
                    tvNumReport.text = dataReport.numReport.toString()

                    imvUpVote.setOnClickListener {
                        viewReportPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onUpdateNumReport(dataReport._id.toString())
                        tvNumReport.text = (dataReport.numReport!!.toInt() + 1).toString()
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
//                    Toast.makeText(this, "Remove Report", Toast.LENGTH_SHORT).show()

                            // Chạy audio
                            if (AppController.soundMode == 1) {
                                mAudioPlayer.play(this, R.raw.xoa_bao_hieu)
                            }

                            mPopupWindowReport!!.dismiss()
                            curMarkerReport = null

                            val inflater2 = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val viewDeletePopup = inflater2.inflate(R.layout.confirm_delete_report_dialog_layout, null)
                            mPopupWindowDelete = PopupWindow(viewDeletePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            mPopupWindowDelete!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)
                            val btnYes = viewDeletePopup.findViewById<Button>(R.id.btnYes_confirm_delete_report)
                            val btnNo = viewDeletePopup.findViewById<Button>(R.id.btnNo_confirm_delete_report)
                            val layoutOutside = viewDeletePopup.findViewById<LinearLayout>(R.id.bg_to_remove_confirm_delete_report)
                            btnYes.setOnClickListener {
                                viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                mPopupWindowDelete!!.dismiss()
                                onDeleteReport(dataReport._id.toString())
                            }

                            btnNo.setOnClickListener {
                                viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                mPopupWindowDelete!!.dismiss()
                            }

                            layoutOutside.setOnClickListener {
                                viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                mPopupWindowDelete!!.dismiss()
                            }
                        } else {
//                    Toast.makeText(this, "Down Vote", Toast.LENGTH_SHORT).show()
                            onUpdateNumDelete(dataReport._id.toString())
                            mPopupWindowReport!!.dismiss()
                            curMarkerReport = null
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
            } else {
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
                val imvUpVote = viewReportPopup.findViewById<LinearLayout>(R.id.imvUpVote_marker_report)
                val imvDownVote = viewReportPopup.findViewById<ImageView>(R.id.imvDownVote_marker_report)
                val imvRecord = viewReportPopup.findViewById<ImageView>(R.id.imRecord_marker_report)
                val imvImage = viewReportPopup.findViewById<ImageView>(R.id.imImage_marker_report)
                val tvNumReport = viewReportPopup.findViewById<TextView>(R.id.tvNumReport_marker_report)

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
                try {
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

                } catch (ex: Exception) {
                }



                tvDescription.text = dataReport.description.toString()
                when (dataReport.type) {
                    "traffic" -> {
                        imvType.background = getDrawable(R.drawable.bg_btn_report_traffic)
                        when (dataReport.subtype1) {
                            "moderate" -> {
                                imvType.setImageResource(R.drawable.ic_report_traffic_moderate)
                                tvType.text = "Kẹt xe vừa"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.ket_xe_vua)
                                }
                            }
                            "heavy" -> {
                                imvType.setImageResource(R.drawable.ic_report_traffic_heavy)
                                tvType.text = "Kẹt xe nặng"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.ket_xe_nang)
                                }
                            }
                            "standstill" -> {
                                imvType.setImageResource(R.drawable.ic_report_traffic_standstill)
                                tvType.text = "Kẹt xe cứng"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.ket_xe_cung)
                                }
                            }
                        }
                    }
                    "crash" -> {
                        imvType.background = getDrawable(R.drawable.bg_btn_report_crash)
                        when (dataReport.subtype1) {
                            "minor" -> {
                                imvType.setImageResource(R.drawable.ic_accident_minor)
                                tvType.text = "Tai nạn nhỏ"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.tai_nan_nho)
                                }
                            }
                            "major" -> {
                                imvType.setImageResource(R.drawable.ic_accident_major)
                                tvType.text = "Tai nạn nghiêm trọng"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.tai_nan_nghiem_trong)
                                }
                            }
                            "other_side" -> {
                                imvType.setImageResource(R.drawable.ic_accident_other_side)
                                tvType.text = "Tai nạn bên đường"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.tai_nan_ben_duong)
                                }
                            }
                        }
                    }
                    "hazard" -> {
                        imvType.background = getDrawable(R.drawable.bg_btn_report_hazard)
                        when (dataReport.subtype2) {
                            "object" -> {
                                imvType.setImageResource(R.drawable.ic_report_hazard_object)
                                tvType.text = "Vật cản"
                                // Chạy audio
                                if (AppController.soundMode == 1) {
                                    mAudioPlayer.play(this, R.raw.vat_can)
                                }
                            }
                            "construction" -> {
                                imvType.setImageResource(R.drawable.ic_report_hazard_construction)
                                tvType.text = "Công trình"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.cong_trinh)
                                }
                            }
                            "broken_light" -> {
                                imvType.setImageResource(R.drawable.ic_report_broken_traffic_light)
                                tvType.text = "Đèn báo hư"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.den_bao_hu)
                                }
                            }
                            "pothole" -> {
                                imvType.setImageResource(R.drawable.ic_report_hazard_pothole)
                                tvType.text = "Hố voi"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.ho_voi)
                                }
                            }
                            "vehicle_stop" -> {
                                imvType.setImageResource(R.drawable.ic_report_hazard_stopped)
                                if (dataReport.subtype1 == "on_road") {
                                    tvType.text = "Xe đậu"
                                    // Chạy audio
                                    if (AppController.soundMode == 1) {
                                        mAudioPlayer.play(this, R.raw.xe_dau)
                                    }
                                }
                                if (dataReport.subtype1 == "shoulder") {
                                    tvType.text = "Xe đậu bên lề"
                                    // Chạy audio
                                    if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                        mAudioPlayer.play(this, R.raw.xe_dau_ben_le)
                                    }
                                }
                            }
                            "road_kill" -> {
                                imvType.setImageResource(R.drawable.ic_report_hazard_roadkill)
                                tvType.text = "Động vật chết"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.dong_vat_chet_tren_duong)
                                }
                            }
                            "animal" -> {
                                imvType.setImageResource(R.drawable.ic_report_hazard_animals)
                                tvType.text = "Động vật nguy hiểm"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.dong_vat_nguy_hiem)
                                }
                            }
                            "missing_sign" -> {
                                imvType.setImageResource(R.drawable.ic_report_hazard_missingsign)
                                tvType.text = "Thiếu biển báo"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.thieu_bien_bao)
                                }
                            }
                            "fog" -> {
                                imvType.setImageResource(R.drawable.ic_hazard_weather_fog)
                                tvType.text = "Sương mù"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.suong_mu)
                                }
                            }
                            "hail" -> {
                                imvType.setImageResource(R.drawable.ic_hazard_weather_hail)
                                tvType.text = "Mưa đá"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.mua_da)
                                }
                            }
                            "flood" -> {
                                imvType.setImageResource(R.drawable.ic_hazard_weather_flood)
                                tvType.text = "Lũ lụt"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.lu_lut)
                                }
                            }
                            "ice" -> {
                                imvType.setImageResource(R.drawable.ic_hazard_weather_ice)
                                tvType.text = "Đá trơn"
                                // Chạy audio
                                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                                    mAudioPlayer.play(this, R.raw.da_tron_tren_duong)
                                }
                            }
                        }
                    }
                }

                curMarkerReport = marker

                // Số report
                tvNumReport.text = dataReport.numReport.toString()

                imvUpVote.setOnClickListener {
                    viewReportPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                Toast.makeText(this, "Up Vote", Toast.LENGTH_SHORT).show()

//                mPopupWindowReport!!.dismiss()
//                curMarkerReport = null
                    onUpdateNumReport(dataReport._id.toString())
                    tvNumReport.text = (dataReport.numReport!!.toInt() + 1).toString()
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
//                    Toast.makeText(this, "Remove Report", Toast.LENGTH_SHORT).show()

                        // Chạy audio
                        if (AppController.soundMode == 1) {
                            mAudioPlayer.play(this, R.raw.xoa_bao_hieu)
                        }

                        mPopupWindowReport!!.dismiss()
                        curMarkerReport = null

//                    val inflater2 = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                    val viewDeletePopup = inflater2.inflate(R.layout.confirm_delete_report_dialog_layout, null)
//                    mPopupWindowDelete = PopupWindow(viewDeletePopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                    mPopupWindowDelete!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)
//                    val btnYes = viewDeletePopup.findViewById<Button>(R.id.btnYes_confirm_delete_report)
//                    val btnNo = viewDeletePopup.findViewById<Button>(R.id.btnNo_confirm_delete_report)
//
//                    btnYes.setOnClickListener {
//                        viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                        mPopupWindowDelete!!.dismiss()
//                        onDeleteReport(dataReport._id.toString())
//                    }
//
//                    btnNo.setOnClickListener {
//                        viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                        mPopupWindowDelete!!.dismiss()
//                    }

                        // Hiện nhưng còn viền trắng
//                    val deleteDialog : Dialog = Dialog(this)
//                    deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//                    deleteDialog.setCancelable(true)
//                    deleteDialog.setContentView(R.layout.confirm_delete_report_dialog_layout)
//                    val btnYes = deleteDialog.findViewById<Button>(R.id.btnYes_confirm_delete_report)
//                    val btnNo = deleteDialog.findViewById<Button>(R.id.btnNo_confirm_delete_report)
//
//                    btnYes.setOnClickListener {
//                        it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                        deleteDialog.dismiss()
//                        onDeleteReport(dataReport._id.toString())
//                    }
//
//                    btnNo.setOnClickListener {
//                        it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                        deleteDialog.dismiss()
//                    }
//                    deleteDialog.show()

                        val inflater2 = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val viewDeletePopup = inflater2.inflate(R.layout.confirm_delete_report_dialog_layout, null)
                        mPopupWindowDelete = PopupWindow(viewDeletePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        mPopupWindowDelete!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)
                        val btnYes = viewDeletePopup.findViewById<Button>(R.id.btnYes_confirm_delete_report)
                        val btnNo = viewDeletePopup.findViewById<Button>(R.id.btnNo_confirm_delete_report)
                        val layoutOutside = viewDeletePopup.findViewById<LinearLayout>(R.id.bg_to_remove_confirm_delete_report)
                        btnYes.setOnClickListener {
                            viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            mPopupWindowDelete!!.dismiss()
                            onDeleteReport(dataReport._id.toString())
                        }

                        btnNo.setOnClickListener {
                            viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            mPopupWindowDelete!!.dismiss()
                        }

                        layoutOutside.setOnClickListener {
                            viewDeletePopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            mPopupWindowDelete!!.dismiss()
                        }
                    } else {
//                    Toast.makeText(this, "Down Vote", Toast.LENGTH_SHORT).show()
                        onUpdateNumDelete(dataReport._id.toString())
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

        }
        if (marker.title == "user") {
//            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            val viewUserPopup = inflater.inflate(R.layout.marker_user_layout, null)
//            mPopupWindowUser = PopupWindow(viewUserPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            mPopupWindowUser!!.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)
//
//            val imvAvatar = viewUserPopup.findViewById<ImageView>(R.id.imvAvatar_marker_user)
//            val tvName = viewUserPopup.findViewById<TextView>(R.id.tvName_marker_user)
////            val tvDOB = viewReportPopup.findViewById<TextView>(R.id.tvDOB_marker_user)
//            val tvEmail = viewUserPopup.findViewById<TextView>(R.id.tvEmail_marker_user)
//            val btnHello = viewUserPopup.findViewById<Button>(R.id.btnHello_marker_user)
//
//            val btnConfirm = viewUserPopup.findViewById<LinearLayout>(R.id.layoutConfirm_marker_user)
//            val imvType = viewUserPopup.findViewById<ImageView>(R.id.imvType_marker_user)
//            val tvType = viewUserPopup.findViewById<TextView>(R.id.tvType_marker_user)
//            val imvInstruction = viewUserPopup.findViewById<ImageView>(R.id.imInstruction_marker_user)
//
//            val dataUser: User = marker.tag as User
//            tvName.text = dataUser.name.toString()
////            tvDOB.text = dataUser.birthDate.toString()
//            tvEmail.text = dataUser.email.toString()
//            btnConfirm.visibility = View.INVISIBLE

            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewUserPopup = inflater.inflate(R.layout.marker_user_layout, null)
            mPopupWindowUser = PopupWindow(viewUserPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mPopupWindowUser!!.showAtLocation(this.currentFocus, Gravity.BOTTOM, 0, 0)

            val imvColor = viewUserPopup.findViewById<ImageView>(R.id.imColor_marker_user)
            val tvName = viewUserPopup.findViewById<TextView>(R.id.tvName_marker_user)
            val tvTypeCar = viewUserPopup.findViewById<TextView>(R.id.tvTypeCar_marker_user)
            val btnHello = viewUserPopup.findViewById<Button>(R.id.btnHello_marker_user)

            val btnConfirm = viewUserPopup.findViewById<LinearLayout>(R.id.layoutConfirm_marker_user)
            val imvType = viewUserPopup.findViewById<ImageView>(R.id.imvType_marker_user)
            val tvType = viewUserPopup.findViewById<TextView>(R.id.tvType_marker_user)
            val imvInstruction = viewUserPopup.findViewById<ImageView>(R.id.imInstruction_marker_user)

            val dataUser: User = marker.tag as User
            tvName.text = dataUser.name.toString()

            if (dataUser.typeCar != "") {
                when (dataUser.typeCar) {
                    "xe con" -> {
                        tvTypeCar.text = "Xe con " + dataUser.modelCar.toString()
                    }
                    "xe tai" -> {
                        tvTypeCar.text = "Xe tải " + dataUser.modelCar.toString()
                    }
                    "xe khach" -> {
                        tvTypeCar.text = "Xe khách " + dataUser.modelCar.toString()
                    }
                    "xe container" -> {
                        tvTypeCar.text = "Xe container " + dataUser.modelCar.toString()
                    }
                }
            }
            if (dataUser.colorCar != "") {
                imvColor.setBackgroundColor(Color.parseColor(dataUser.colorCar))
            }

            btnConfirm.visibility = View.INVISIBLE


            curMarkerUser = marker
            btnHello.setOnClickListener {
                if (AppController.settingSocket == "true") {
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this, R.raw.gui_loi_chao)
                    }

                    attemptHello(AppController.userProfile?.name.toString(), dataUser.socketID.toString())
                    mPopupWindowUser!!.dismiss()
                    curMarkerUser = null
                    viewUserPopup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                } else {
                    TastyToast.makeText(this, "Bạn phải bật chết độ giao tiếp với tài xế để có thể thực hiện thao tác này!", TastyToast.LENGTH_LONG, TastyToast.INFO).show()
                }
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
//                    if (fingers == 4 && gestureDistance >= 120) {
//                        // Báo nên quay đầu
//                        btnConfirm.visibility = View.VISIBLE
//                        mType = 4
//                        imvType.setImageResource(R.drawable.ic_report_turn_around_44dp)
//                        tvType.text = "NGUY HIỂM NÊN QUAY ĐẦU"
//                    }
                    return false
                }

                override fun onSwipeUp(fingers: Int, gestureDuration: Long, gestureDistance: Double): Boolean {
//                    Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  up " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    if(fingers == 2 && gestureDistance <= 100){
//                        Toast.makeText(this@MainActivity, "You swiped " + fingers + " fingers  up " + gestureDuration + " milliseconds " + gestureDistance + " pixels far", Toast.LENGTH_SHORT).show()
//                    }
                    btnConfirm.visibility = View.INVISIBLE
                    mType = 0
                    if (fingers == 2 && gestureDistance >= 120) {
                        // Báo nên quay đầu
                        btnConfirm.visibility = View.VISIBLE
                        mType = 4
                        imvType.setImageResource(R.drawable.ic_report_turn_around_44dp)
                        tvType.text = "NGUY HIỂM NÊN QUAY ĐẦU"
                    }
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
                if (AppController.settingSocket == "true") {
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
                } else {
                    TastyToast.makeText(this, "Bạn phải bật chết độ giao tiếp với tài xế để có thể thực hiện thao tác này!", TastyToast.LENGTH_LONG, TastyToast.INFO).show()
                }
            }

            imvInstruction.setOnClickListener {
                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewPopup = inflater.inflate(R.layout.marker_user_instruction_dialog_layout, null)

                val mPopupWindow = PopupWindow(viewPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                mPopupWindow.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val btnClose = viewPopup.findViewById<ImageView>(R.id.btnClose_marker_user_instruction_dialog_layout)

                btnClose.setOnClickListener {
                    mPopupWindow.dismiss()
                }
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
        val call = service.getNearbyUsers(lastLocation.latitude, lastLocation.longitude, AppController.settingUserRadius!!.toFloat())
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
                // Bỏ vì bớt toast
//                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
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
                    if (listUser[i].email != AppController.userProfile!!.email && listUser[i].status != "invisible") {
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
                    if (listUser[i].email != AppController.userProfile!!.email && listUser[i]._id != dataUser._id && listUser[i].status != "invisible") {
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
//        val random = Random()
//        markerOptions.position(LatLng(user.currentLocation!!.coordinates!![1], user.currentLocation!!.coordinates!![0]))
//        markerOptions.title("user")
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(random.nextFloat() * 360))
//        val marker = mMap.addMarker(markerOptions)
//        listUserMarker.add(marker)
//        marker.tag = user

        markerOptions.position(LatLng(user.currentLocation!!.coordinates!![1], user.currentLocation!!.coordinates!![0]))
        markerOptions.title("user")
        when (user.typeCar) {
            "xe con" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_car_44dp))
            }
            "xe tai" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_truck_44dp))
            }
            "xe khach" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_bus_44dp))
            }
            "xe container" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_container_44dp))
            }
            else -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_other_car_44dp))
        }
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_other_car_44dp))
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
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

    private fun onUpdateMyCar(user: User) {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.updateMyCar(user)
        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {

                } else {
                    val apiError = ErrorUtils.parseError(response)
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

    private fun onUpdateStatus(user: User) {
        val service = APIServiceGenerator.createService(UserService::class.java)
        val call = service.updateStatus(user)
        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {

                } else {
                    val apiError = ErrorUtils.parseError(response)
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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
            // Bỏ vì bớt toast
//            TastyToast.makeText(this.applicationContext,
//                    "Đã kết nối socket", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
            // Gán socket ID vào cho socketID của người dùng
            AppController.userProfile?.socketID = socket.id()
            onUpdateSocketID(AppController.userProfile!!)
        }
    }

    private val onDisconnect = Emitter.Listener {
        this.runOnUiThread {
            // Bỏ vì bớt toast
//            TastyToast.makeText(this.applicationContext,
//                    "Ngắt kết nối socket", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
        }
    }

    private val onConnectError = Emitter.Listener {
        this.runOnUiThread {
            // Bỏ vì bớt toast
//            TastyToast.makeText(this.applicationContext,
//                    "Lỗi kết nối socket", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.tai_xe_da_chao_ban_chuc_thuong_lo_binh_an)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewHelloPopup = inflater.inflate(R.layout.hello_dialog_layout, null)
                // Dùng layout cũ
//                mPopupWindowHello = PopupWindow(viewHelloPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                // Layout mới
                mPopupWindowHello = PopupWindow(viewHelloPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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
                    // Chạy audio
                    if (AppController.soundMode == 1) {
                        mAudioPlayer.play(this, R.raw.gui_loi_chao)
                    }
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

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.canh_bao_ha_do_sang_den_pha)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnStrongLightPopup = inflater.inflate(R.layout.warn_strong_light_dialog_layout, null)
                // Dùng với layout cũ
//                mPopupWindowHello = PopupWindow(viewWarnStrongLightPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                // Layout mới
                mPopupWindowHello = PopupWindow(viewWarnStrongLightPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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

    @SuppressLint("InflateParams")
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

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.canh_bao_co_giam_sat_gan_do)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnWatcherPopup = inflater.inflate(R.layout.warn_watcher_dialog_layout, null)
                // Dùng với layout cũ
//                mPopupWindowHello = PopupWindow(viewWarnWatcherPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                // Layout mới
                mPopupWindowHello = PopupWindow(viewWarnWatcherPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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

    @SuppressLint("InflateParams")
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

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.canh_bao_nguy_hiem_nen_giam_toc_do)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnSlowDownPopup = inflater.inflate(R.layout.warn_slow_down_dialog_layout, null)
                // Dùng với layout cũ
//                mPopupWindowHello = PopupWindow(viewWarnSlowDownPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                // Layout mới
                mPopupWindowHello = PopupWindow(viewWarnSlowDownPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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

    @SuppressLint("InflateParams")
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

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.canh_bao_nguy_hiem_nen_quay_dau_xe)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnTurnAroundPopup = inflater.inflate(R.layout.warn_turn_around_dialog_layout, null)
                // Dùng với layout cũ
//                mPopupWindowHello = PopupWindow(viewWarnTurnAroundPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                // Layout mới
                mPopupWindowHello = PopupWindow(viewWarnTurnAroundPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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

    @SuppressLint("InflateParams")
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

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.tai_xe_da_cam_on_ban)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewWarnThankPopup = inflater.inflate(R.layout.warn_thank_dialog_layout, null)
                // Dùng với layout cũ
//                mPopupWindowHello = PopupWindow(viewWarnThankPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                // Layout mới
                mPopupWindowHello = PopupWindow(viewWarnThankPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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

    @SuppressLint("InflateParams")
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
//                Toast.makeText(this, "Đã vào", Toast.LENGTH_SHORT).show()

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.canh_bao_co_tai_xe_chay_au)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewReportOtherPopup = inflater.inflate(R.layout.report_other_dialog_layout, null)
                // Dùng với layout cũ
//                mPopupWindowHello = PopupWindow(viewReportOtherPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                // Layout mới
                mPopupWindowHello = PopupWindow(viewReportOtherPopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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

                // Chạy audio
                if (AppController.soundMode == 1 || AppController.soundMode == 2) {
                    mAudioPlayer.play(this, R.raw.canh_bao_co_bo_cau)
                }

                val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewReportOtherPopup = inflater.inflate(R.layout.report_other_other_dialog_layout, null)
                mPopupWindowHello = PopupWindow(viewReportOtherPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mPopupWindowHello!!.showAtLocation(this.currentFocus, Gravity.CENTER, 0, 0)

                val tvEmail = viewReportOtherPopup.findViewById<TextView>(R.id.tvEmail_report_other_other_dialog)
                val imImage = viewReportOtherPopup.findViewById<ImageView>(R.id.imImage_report_other_other_dialog)
                val btnLicensePlate = viewReportOtherPopup.findViewById<Button>(R.id.btnLicensePlate_report_other_other_dialog)
                val imPicture = viewReportOtherPopup.findViewById<ImageView>(R.id.imPicture_report_other_other_dialog)
                val btnClose = viewReportOtherPopup.findViewById<Button>(R.id.btnClose_report_other_other_dialog)
                val tvMess = viewReportOtherPopup.findViewById<TextView>(R.id.tvMess_report_other_other_dialog)

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
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                // Bỏ vì bớt toast
//                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
                t.printStackTrace()
            }
        })
    }

    private fun onGetNearbyReports() {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.getNearbyReports(lastLocation.latitude, lastLocation.longitude, AppController.settingReportRadius!!.toFloat())
        call.enqueue(object : Callback<NearbyReportsResponse> {
            override fun onResponse(call: Call<NearbyReportsResponse>, response: Response<NearbyReportsResponse>) {
                if (response.isSuccessful) {
                    // Toast.makeText(this@MainActivity, "Phạm vi 3 km", Toast.LENGTH_SHORT).show()
                    onNearbyReportsSuccess(response.body()!!)
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<NearbyReportsResponse>, t: Throwable) {
                // Bỏ vì bớt toast
//                TastyToast.makeText(this@MainActivity, "Không có kết nối Internet", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
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
            "other" -> {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_bar_report_other))
            }
        }
        val marker = mMap.addMarker(markerOptions)
        listReportMarker.add(marker)
        marker.tag = report
    }

    private fun onDeleteReport(reportID: String) {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.deleteReport(reportID)
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@MainActivity, "Xoá báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                } else {
                    val apiError = ErrorUtils.parseError(response)
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
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

    private fun onUpdateNumReport(id: String) {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.updateNumReport(id)
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@MainActivity, "Bình chọn báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
//                    Toast.makeText(this@ReportCrashActivity, "XOng 2", Toast.LENGTH_SHORT).show()

                } else {
                    val apiError = ErrorUtils.parseError(response)
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
    }

    private fun onUpdateNumDelete(id: String) {
        val service = APIServiceGenerator.createService(ReportService::class.java)
        val call = service.updateNumDelete(id)
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    TastyToast.makeText(this@MainActivity, "Yêu cầu bỏ báo cáo thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
//                    Toast.makeText(this@ReportCrashActivity, "XOng 2", Toast.LENGTH_SHORT).show()

                } else {
                    val apiError = ErrorUtils.parseError(response)
                    // Bỏ vì bớt toast
//                    TastyToast.makeText(this@MainActivity, "Lỗi: " + apiError.message(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show()
                }
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                Log.e("Failure", "Error: " + t.message)
            }
        })
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
            if (e1!!.x < e2!!.x) {
                Log.d(TAG, "Left to Right swipe performed")
            }

            if (e1.x > e2.x) {
                Log.d(TAG, "Right to Left swipe performed")
            }

            if (e1.y < e2.y) {
                Log.d(TAG, "Up to Down swipe performed")
            }

            if (e1.y > e2.y) {
                Log.d(TAG, "Down to Up swipe performed")
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

    private fun initStepRecyclerView(myDataSet: Route, view: View) {
        val viewManagerStep = LinearLayoutManager(this)
        val viewAdapterStep = StepAdapter(getStepSet(myDataSet))

        val recyclerViewStep = view.findViewById<RecyclerView>(R.id.recycler_view_steps_layout)

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerViewStep.setHasFixedSize(true)

        // use a linear layout manager
        recyclerViewStep.layoutManager = viewManagerStep

        // specify an viewAdapterStep (see also next example)
        recyclerViewStep.adapter = viewAdapterStep
    }

    private lateinit var mItemTouchHelper: ItemTouchHelper

    //<<<<<<< HEAD
    private lateinit var viewAdapterEditDirection: PlaceAdapter
//=======
//    private fun initDirectionRecyclerView(myDataSet: ArrayList<SimplePlace>, view: View, btnAdd: ImageView) {
//        val viewManagerEditDirection = LinearLayoutManager(this)
//        val viewAdapterEditDirection = PlaceAdapter(myDataSet, this)
//
//        val recyclerViewEditDirection = view.findViewById<RecyclerView>(R.id.recycler_view_edit_direction_layout).apply {
//            // use this setting to improve performance if you know that changes
//            // in content do not change the layout size of the RecyclerView
//            setHasFixedSize(true)
//
//            // use a linear layout manager
//            layoutManager = viewManagerEditDirection
//
//            // specify an viewAdapterStep (see also next example)
//            adapter = viewAdapterEditDirection
//        }
//        val callback = SimpleItemTouchHelperCallback(viewAdapterEditDirection)
//        mItemTouchHelper = ItemTouchHelper(callback)
//        mItemTouchHelper.attachToRecyclerView(recyclerViewEditDirection)
//
//        btnAdd.setOnClickListener {
//            showAddPlacePopup(myDataSet, viewAdapterEditDirection)
//        }
//    }
//>>>>>>> e8cbadd32c29eacb3b8bd84c866e300385e38764

    private fun initDirectionRecyclerView(myDataSet: ArrayList<SimplePlace>, view: View, btnAdd: TextView) {
        val viewManagerEditDirection = LinearLayoutManager(this)
        viewAdapterEditDirection = PlaceAdapter(myDataSet, this)

        val recyclerViewEditDirection = view.findViewById<RecyclerView>(R.id.recycler_view_edit_direction_layout).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManagerEditDirection

            // specify an viewAdapterStep (see also next example)
            adapter = viewAdapterEditDirection
        }
        val callback = SimpleItemTouchHelperCallback(viewAdapterEditDirection)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(recyclerViewEditDirection)

        btnAdd.setOnClickListener {
            showAddPlacePopup(myDataSet, viewAdapterEditDirection)
        }
    }

    private fun getStepSet(route: Route): ArrayList<Step> {
        val stepSet: ArrayList<Step> = ArrayList()
        for (iL in 0 until route.legs!!.size) {
            for (iS in 0 until route.legs!![iL].steps!!.size) {
                stepSet.add(route.legs!![iL].steps!![iS])
            }
        }
        return stepSet
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper.startDrag(viewHolder)
    }

    private var nearbyPlacesResultMarkers: ArrayList<Marker> = ArrayList()

    private fun getNearbyPlaces(type: String, location: Location, radius: Int) {
        // Build retrofit
        val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        val service = retrofit.create(NearbyPlacesInterface::class.java)
        // Send request
        val call = service.getNearbyPlaces(type, location.latitude.toString() + "," + location.longitude.toString(), radius)
        call.enqueue(object : Callback<NearbyPlacesResponse> {
            override fun onFailure(call: Call<NearbyPlacesResponse>?, t: Throwable?) {
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(call: Call<NearbyPlacesResponse>?, response: Response<NearbyPlacesResponse>) {
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 14f))
                    nearbyPlacesResultMarkers.clear()
                    for (i in 0 until response.body()!!.results.size) {
                        val name = response.body()!!.results[i].name
                        val lat = response.body()!!.results[i].geometry.location.lat
                        val lng = response.body()!!.results[i].geometry.location.lng
                        val latLng = LatLng(lat, lng)
                        val place = SimplePlace(name, LatLng(lat, lng))
                        val markerOption = MarkerOptions()
                        markerOption.position(latLng)
                        markerOption.title("nearby_places_result")
                        val marker = mMap.addMarker(markerOption)
                        marker.tag = place
                        nearbyPlacesResultMarkers.add(marker)
                    }

                } catch (e: Exception) {
                    Log.d("onResponse", "There is an error")
                    e.printStackTrace()
                }
            }
        })

        // Handle response
    }
}
