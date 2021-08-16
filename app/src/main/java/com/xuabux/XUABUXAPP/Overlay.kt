package com.xuabux.XUABUXAPP

import afu.org.checkerframework.checker.nullness.qual.NonNull
import android.graphics.Color
import kotlin.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.xuabux.XUABUXAPP.R
import org.json.JSONException
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.directions.route.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.internal.i
import com.google.android.gms.maps.model.Polyline

import com.google.android.material.snackbar.Snackbar

import com.google.android.libraries.places.internal.e
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.PolylineOptions
import com.google.common.collect.Iterables.size
import java.nio.file.Files.size


class Overlay :  AppCompatActivity(), OnMapReadyCallback, RoutingListener {
    var PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private lateinit var mMap: GoogleMap
    private var lastKnownLocation: Location? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    private fun getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
        val perthLocation = LatLng(-31.90, 115.86)
        val perth: Marker = mMap.addMarker(
            MarkerOptions()
                .position(perthLocation)
                .visible(false)
        )
        mMap.setOnMapLongClickListener { latLng ->
            perth.isVisible=true
            perth.position=latLng
            val bld = LatLngBounds.Builder()
            val ll = lastKnownLocation?.let {
                LatLng(
                    it.latitude,
                    it.longitude
                )
            }
            bld.include(ll);
            bld.include(perth.position);
            val bounds = bld.build()

            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 70)
            )

        }
        Places.initialize(applicationContext, "AIzaSyCdl-NC0egFdJGNKzr_0szdcQKiWVXNEto");
        var placesClient = Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.

// Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                perth.isVisible=true
                perth.position=place.latLng
                val bld = LatLngBounds.Builder()
                val ll = lastKnownLocation?.let {
                    LatLng(
                        it.latitude,
                        it.longitude
                    )
                }
                bld.include(ll);
                bld.include(perth.position);
                val bounds = bld.build()

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, 70)
                )
            }

            override fun onError(status: Status) {

                val toast = Toast.makeText(applicationContext, status.statusMessage, Toast.LENGTH_LONG)
                toast.show()
            }

        })
        //mMap.isMyLocationEnabled = true;
        // Add a marker in Sydney and move the camera
        val nachoPos = LatLng(4.6381991,-74.0862351)
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
        layer.setOnFeatureClickListener { feature ->
            Log.i("GeoJsonClick", "Feature clicked: ${feature.getProperty("NOMB_TRAMO")}")
        }

        layer.addLayerToMap()
        getLocationPermission()
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()




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

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d("XuaDebug", "Current location is null. Using defaults.")
                        Log.e("XuaDebug", "Exception: %s", task.exception)
                        mMap?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

    var myLocation: Location? = null
    var destinationLocation: Location? = null
    protected var start: LatLng? = null
    protected var end: LatLng? = null
    private val LOCATION_REQUEST_CODE = 23
    var locationPermission = false
    private var polylines: List<Polyline>? = null
    fun Findroutes(Start: LatLng?, End: LatLng?) {
        if (Start == null || End == null) {
            Toast.makeText(this@Overlay, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {
            val routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(Start, End)
                .key("AIzaSyCcynBp2TY3JVT20q1K25a0VuRxxAfMc9k")
                .build()
            routing.execute()
        }
    }

    override fun onRoutingFailure(p0: RouteException?) {
        val parentLayout = findViewById<View>(android.R.id.content)
        val snackbar: Snackbar = Snackbar.make(parentLayout, toString(), Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    override fun onRoutingStart() {
        Toast.makeText(this,"Finding Route...",Toast.LENGTH_LONG).show()
    }

    override fun onRoutingSuccess(p0: ArrayList<Route>?, p1: Int) {
        val route = p0
        val shortestRouteIndex = p1
        var i =0
        val center = CameraUpdateFactory.newLatLng(start)
        val zoom = CameraUpdateFactory.zoomTo(16f)
        if(polylines!=null) {
            polylines = null
        }
        val polyOptions = PolylineOptions()
        val polylineStartLatLng: LatLng? = null
        val polylineEndLatLng: LatLng? = null
        polylines = ArrayList()
        val rz= route?.size
        for ((i,value)in route?.withIndex()!!) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(3)
                polyOptions.width(5F)
                polyOptions.addAll(route?.get(shortestRouteIndex)?.getPoints());
                val polyline =  mMap.addPolyline(polyOptions)
                val polylineStartLatLng=polyline.getPoints().get(0);
                val k=polyline.getPoints().size
                val polylineEndLatLng=polyline.getPoints().get(k-1);
                (polylines as ArrayList<Polyline>).add(polyline);

            }

    }
     fun onConnectionFailed() {
        Findroutes(start,end)
    }

}

    override fun onRoutingCancelled() {
        Findroutes(start,end)
    }
}


