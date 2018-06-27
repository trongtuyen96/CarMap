package com.example.trongtuyen.carmap.models.direction

import com.google.android.gms.maps.model.LatLng

class Leg {
    var distance: Distance? = null
    var duration: Duration? = null
    var endAddress: String? = null
    var endLocation: LatLng? = null
    var startAddress: String? = null
    var startLocation: LatLng? = null

    var steps: ArrayList<Step>? = null
}