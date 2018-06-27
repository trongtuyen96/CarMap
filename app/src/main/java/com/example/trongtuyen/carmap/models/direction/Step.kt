package com.example.trongtuyen.carmap.models.direction

import com.google.android.gms.maps.model.LatLng

class Step {
    var distance: Distance? = null
    var duration: Duration? = null
    var endLocation: LatLng? = null
    var startLocation: LatLng? = null

    var points: List<LatLng>? = null
    var instruction: String ?= null
}