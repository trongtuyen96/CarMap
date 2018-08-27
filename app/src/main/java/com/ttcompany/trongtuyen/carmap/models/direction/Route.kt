package com.ttcompany.trongtuyen.carmap.models.direction

import com.google.android.gms.maps.model.LatLng

class Route {
    var summary: String?=null
    var distance: Distance? = null
    var duration: Duration? = null
    var endAddress: String? = null
    var endLocation: LatLng? = null
    var startAddress: String? = null
    var startLocation: LatLng? = null

    var points: List<LatLng>? = null
    var legs: ArrayList<Leg>? = null
}