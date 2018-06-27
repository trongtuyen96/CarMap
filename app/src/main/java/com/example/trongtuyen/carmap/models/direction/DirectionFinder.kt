package com.example.trongtuyen.carmap.models.direction

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import android.text.Html
import android.util.Log
import com.example.trongtuyen.carmap.R.string.google_maps_key
import com.google.android.gms.maps.model.LatLng
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList


class DirectionFinder(private val listener: DirectionListener, private val origin: String, private val destination: String) {
    companion object {
        private const val DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?"
        private const val GOOGLE_API_KEY = "AIzaSyDTWxpGP0Zjgifxrau0BrNdzebFmuUKEpI"
        private const val TAG = "DirectionFinder"
    }

    interface DirectionListener {
        fun onDirectionFinderStart()
        fun onDirectionFinderSuccess(routes: List<Route>)
    }

    @Throws(UnsupportedEncodingException::class)
    fun execute() {
        listener.onDirectionFinderStart()
        DownloadRawData().execute(createUrl())
    }

    @Throws(UnsupportedEncodingException::class)
    private fun createUrl(): String {
        var url = DIRECTION_URL_API
        url += "origin=" + URLEncoder.encode(origin, "utf-8")
        url += "&destination=" + URLEncoder.encode(destination, "utf-8")
        url += "&alternatives=true"
        url += "&language=vi"

        url += "&key=$GOOGLE_API_KEY"
        Log.v(TAG, url)
        return url
    }

    @SuppressLint("StaticFieldLeak")
    private inner class DownloadRawData : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String? {
            val link = params[0]
            try {
                val url = URL(link)
                val `is` = url.openConnection().getInputStream()
                val buffer = StringBuffer()
                val reader = BufferedReader(InputStreamReader(`is`))

                var line = reader.readLine()
                while (line != null) {
                    buffer.append(line + "\n")
                    line = reader.readLine()
                }

                return buffer.toString()

            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(res: String) {
            try {
                parseJSon(res)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    @Throws(JSONException::class)
    private fun parseJSon(data: String?) {
        if (data == null)
            return

        val routes = ArrayList<Route>()
        val jsonData = JSONObject(data)
        val jsonRoutes = jsonData.getJSONArray("routes")
        for (iR in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(iR)
            val route = Route()
            route.legs = ArrayList()

            val overview_polylineJson = jsonRoute.getJSONObject("overview_polyline")
            val jsonLegs = jsonRoute.getJSONArray("legs")
            var sumLegDistance = 0
            var sumLegDuration = 0

            for (iL in 0 until jsonLegs.length()) {
                val jsonLeg = jsonLegs.getJSONObject(iL)
                val leg = Leg()
                leg.steps = ArrayList()

                val jsonLegDistance = jsonLeg.getJSONObject("distance")
                val jsonLegDuration = jsonLeg.getJSONObject("duration")
                val jsonLegEndLocation = jsonLeg.getJSONObject("end_location")
                val jsonLegStartLocation = jsonLeg.getJSONObject("start_location")

                val jsonSteps = jsonLeg.getJSONArray("steps")
                for (iS in 0 until jsonSteps.length()) {
                    val jsonStep = jsonSteps.getJSONObject(iS)
                    val step = Step()

                    val jsonStepDistance = jsonStep.getJSONObject("distance")
                    val jsonStepDuration = jsonStep.getJSONObject("duration")
                    val jsonStepEndLocation = jsonStep.getJSONObject("end_location")
                    val jsonStepStartLocation = jsonStep.getJSONObject("start_location")
                    val polylineJson = jsonStep.getJSONObject("polyline")


                    val xmlInstruction = java.net.URLDecoder.decode(jsonStep.getString("html_instructions"), "UTF-8")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        step.instruction = Html.fromHtml(xmlInstruction, Html.FROM_HTML_MODE_LEGACY).toString()
                    } else {
                        step.instruction = Html.fromHtml(xmlInstruction).toString()
                    }

                    step.points = decodePolyLine(polylineJson.getString("points"))
                    step.distance = Distance(jsonStepDistance.getString("text"), jsonStepDistance.getInt("value"))
                    step.duration = Duration(jsonStepDuration.getString("text"), jsonStepDuration.getInt("value"))
                    step.startLocation = LatLng(jsonStepStartLocation.getDouble("lat"), jsonStepStartLocation.getDouble("lng"))
                    step.endLocation = LatLng(jsonStepEndLocation.getDouble("lat"), jsonStepEndLocation.getDouble("lng"))

                    leg.steps!!.add(step)
                }

                leg.distance = Distance(jsonLegDistance.getString("text"), jsonLegDistance.getInt("value"))
                leg.duration = Duration(jsonLegDuration.getString("text"), jsonLegDuration.getInt("value"))

                sumLegDistance += leg.distance!!.value
                sumLegDuration += leg.duration!!.value

                leg.startLocation = LatLng(jsonLegStartLocation.getDouble("lat"), jsonLegStartLocation.getDouble("lng"))
                leg.endLocation = LatLng(jsonLegEndLocation.getDouble("lat"), jsonLegEndLocation.getDouble("lng"))

                leg.endAddress = jsonLeg.getString("end_address")
                leg.startAddress = jsonLeg.getString("start_address")

                route.legs!!.add(leg)
            }



            route.summary = jsonRoute.getString("summary")
            route.points = decodePolyLine(overview_polylineJson.getString("points"))
            route.endAddress = route.legs!![jsonLegs.length() - 1].endAddress
            route.startAddress = route.legs!![0].startAddress
            route.startLocation = route.legs!![0].startLocation
            route.endLocation = route.legs!![jsonLegs.length() - 1].endLocation
            if (jsonLegs.length() == 1) {
                route.distance = route.legs!![0].distance
                route.duration = route.legs!![0].duration
            } else {
                route.distance = Distance(sumLegDistance.toString() + " m", sumLegDistance)
                route.duration = Duration(sumLegDuration.toString() + " s", sumLegDuration)
            }

            routes.add(route)
        }

        listener.onDirectionFinderSuccess(routes)
    }

    private fun decodePolyLine(poly: String): List<LatLng> {
        val len = poly.length
        var index = 0
        val decoded = ArrayList<LatLng>()
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = poly[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = poly[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            decoded.add(LatLng(
                    lat / 100000.0, lng / 100000.0
            ))
        }

        return decoded
    }
}