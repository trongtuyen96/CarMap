package com.example.trongtuyen.carmap.adapters

import android.widget.TextView
import com.google.android.gms.maps.model.Marker
import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.R.id.btnHello
import com.google.android.gms.maps.GoogleMap


class CustomInfoWindowAdapter(private val context: Activity) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = context.layoutInflater.inflate(R.layout.custom_infowindow_layout, null)

        val tvTitle = view.findViewById(R.id.tvName_custom_info_windows) as TextView
        val tvSubTitle = view.findViewById(R.id.tvEmail_custom_info_windows) as TextView

        tvTitle.text = marker.title
        tvSubTitle.text = marker.snippet
        return view
    }
}