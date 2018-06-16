package com.example.trongtuyen.carmap.direction

import com.google.android.gms.maps.model.LatLng

class Route {
    var distance: Distance? = null
    var duration: Duration? = null
    var endAddress: String? = null
    var endLocation: LatLng? = null
    var startAddress: String? = null
    var startLocation: LatLng? = null

    var points: List<LatLng>? = null
}