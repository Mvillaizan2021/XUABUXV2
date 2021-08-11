package com.xuabux.XUABUXAPP

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.xuabux.XUABUXAPP.R
import org.json.JSONException
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*

class Overlay :  AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //mMap.isMyLocationEnabled = true;
        // Add a marker in Sydney and move the camera
        val nachoPos = LatLng(4.6381991,-74.0862351)
        mMap.addMarker(MarkerOptions().position(nachoPos).title("Universidad Nacional De Colombia"))

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    nachoPos.latitude,
                    nachoPos.longitude
                ), 12.0f
            )
        )

        val res = resources
        val `is` = res.openRawResource(R.raw.ciclojson)
        val sc = Scanner(`is`)
        val builder = StringBuilder()
        while (sc.hasNextLine()) {
            builder.append(sc.nextLine())
        }
        val js = parseJson(builder.toString())
        val layer = GeoJsonLayer(mMap, js)
        layer.defaultLineStringStyle.color= Color.parseColor("#74EA56")
        layer.addLayerToMap()





    }
    private fun parseJson(s: String): JSONObject? {
        val SB = StringBuilder()
        try {
            return JSONObject(s)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

}