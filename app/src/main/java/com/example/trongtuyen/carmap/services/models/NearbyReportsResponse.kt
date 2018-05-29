package com.example.trongtuyen.carmap.services.models

import com.example.trongtuyen.carmap.models.Report

class NearbyReportsResponse {
    var success: Boolean? = null
    var reports: List<Report>? = null
    var distances: List<Double>? = null
}