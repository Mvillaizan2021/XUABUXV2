package com.xuabux.XUABUXAPP

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
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.gms.maps.model.PolylineOptions
import java.util.ArrayList
import java.util.HashMap
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception


class Overlay :  AppCompatActivity(), OnMapReadyCallback {


    var txtLatInicio: EditText? =
        null, var txtLongInicio:EditText? = null, var txtLatFinal:EditText? = null, var txtLongFinal:EditText? = null

    var jsonObjectRequest: JsonObjectRequest? = null
    var request: RequestQueue? = null


    null, var longInicial:kotlin.Double? = null, var latFinal:kotlin.Double? = null, var longFinal:kotlin.Double? = null
}
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
    private val currentLatLng: LatLng? = null

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
    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String? {

        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        val sensor = "sensor=false"

        val parameters = "$str_origin&$str_dest&$sensor"

        val output = "json"

        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }
    override fun onMapReady(googleMap: GoogleMap) {
        var center: LatLng? = null
        var points: ArrayList<LatLng>? = null
        var lineOptions: PolylineOptions? = null
        var latInicial: Double? =

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




        //SADSASDFAS


        for (i in 0 until PATH.routes.size()) {
            points = ArrayList<LatLng>()
            lineOptions = PolylineOptions()

            // Obteniendo el detalle de la ruta
            val path: List<HashMap<String, String>> = PATH.routes.get(i)


            for (j in path.indices) {
                val point = path[j]
                val lat = point["lat"]!!.toDouble()
                val lng = point["lng"]!!.toDouble()
                val position = LatLng(lat, lng)
                if (center == null) {

                    center = LatLng(lat, lng)
                }
                points.add(position)
            }


            lineOptions.addAll(points)

            lineOptions.width(2f)

            lineOptions.color(Color.BLUE)
        }

        mMap.addPolyline(lineOptions)

        val origen = LatLng(
            PATH.coordenadas.getLatitudInicial(),
            PATH.coordenadas.getLongitudInicial()
        )
        mMap.addMarker(
            MarkerOptions().position(origen).title(
                "Lat: " + PATH.coordenadas.getLatitudInicial()
                    .toString() + " - Long: " + PATH.coordenadas.getLongitudInicial()
            )
        )

        val destino = LatLng(
            PATH.coordenadas.getLatitudFinal(),
            PATH.coordenadas.getLongitudFinal()
        )
        mMap.addMarker(
            MarkerOptions().position(destino).title(
                "Lat: " + PATH.coordenadas.getLatitudFinal()
                    .toString() + " - Long: " + PATH.coordenadas.getLongitudFinal()
            )
        )

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15f))

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

fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(toolbar)
    txtLatInicio = findViewById(R.id.txtLatIni) as EditText
    txtLongInicio = findViewById(R.id.txtLongIni) as EditText
    txtLatFinal = findViewById(R.id.txtLatFin) as EditText
    txtLongFinal = findViewById(R.id.txtLongFin) as EditText
    val fab: FloatingActionButton = findViewById(R.id.fab) as FloatingActionButton
    fab.setOnClickListener(View.OnClickListener {
        PATH.coordenadas.setLatitudInicial(java.lang.Double.valueOf(txtLatInicio!!.text.toString()))
        PATH.coordenadas.setLongitudInicial(
            java.lang.Double.valueOf(
                txtLongInicio.getText().toString()
            )
        )
        PATH.coordenadas.setLatitudFinal(
            java.lang.Double.valueOf(
                txtLatFinal.getText().toString()
            )
        )
        PATH.coordenadas.setLongitudFinal(
            java.lang.Double.valueOf(
                txtLongFinal.getText().toString()
            )
        )
        webServiceObtenerRuta(
            txtLatInicio!!.text.toString(), txtLongInicio.getText().toString(),
            txtLatFinal.getText().toString(), txtLongFinal.getText().toString()
        )
        val miIntent = Intent(this, Overlay::class.java)
        startActivity(miIntent)
    })
    request = Volley.newRequestQueue(getApplicationContext())
}

private fun webServiceObtenerRuta(
    latitudInicial: String,
    longitudInicial: String,
    latitudFinal: String,
    longitudFinal: String
) {
    val url =
        ("https://maps.googleapis.com/maps/api/directions/json?origin=" + latitudInicial + "," + longitudInicial
                + "&destination=" + latitudFinal + "," + longitudFinal)
    jsonObjectRequest =
        JsonObjectRequest(Request.Method.GET, url, null, object : Listener<JSONObject?>() {
            fun onResponse(response: JSONObject) {
                var jRoutes: JSONArray? = null
                var jLegs: JSONArray? = null
                var jSteps: JSONArray? = null
                try {
                    jRoutes = response.getJSONArray("routes")
                    /** Traversing all routes  */
                    for (i in 0 until jRoutes.length()) {
                        jLegs = (jRoutes[i] as JSONObject).getJSONArray("legs")
                        val path: MutableList<HashMap<String, String>> = ArrayList()
                        /** Traversing all legs  */
                        for (j in 0 until jLegs.length()) {
                            jSteps = (jLegs[j] as JSONObject).getJSONArray("steps")
                            /** Traversing all steps  */
                            for (k in 0 until jSteps.length()) {
                                var polyline = ""
                                polyline =
                                    ((jSteps[k] as JSONObject)["polyline"] as JSONObject)["points"] as String
                                val list = decodePoly(polyline)
                                /** Traversing all points  */
                                for (l in list!!.indices) {
                                    val hm = HashMap<String, String>()
                                    hm["lat"] = java.lang.Double.toString(list[l].latitude)
                                    hm["lng"] = java.lang.Double.toString(list[l].longitude)
                                    path.add(hm)
                                }
                            }
                            PATH.routes.add(path)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                }
            }
        }, object : ErrorListener() {
            fun onErrorResponse(error: VolleyError) {
                Toast.makeText(
                    getApplicationContext(),
                    "No se puede conectar " + error.toString(),
                    Toast.LENGTH_LONG
                ).show()
                println()
                Log.d("ERROR: ", error.toString())
            }
        }
        )
    request.add(jsonObjectRequest)
}
fun parse(jObject: JSONObject): List<List<HashMap<String?, String?>?>?>? {
    //Este método PARSEA el JSONObject que retorna del API de Rutas de Google devolviendo
    //una lista del lista de HashMap Strings con el listado de Coordenadas de Lat y Long,
    //con la cual se podrá dibujar pollinas que describan la ruta entre 2 puntos.
    var jRoutes: JSONArray? = null
    var jLegs: JSONArray? = null
    var jSteps: JSONArray? = null
    try {
        jRoutes = jObject.getJSONArray("routes")
        /** Traversing all routes  */
        for (i in 0 until jRoutes.length()) {
            jLegs = (jRoutes[i] as JSONObject).getJSONArray("legs")
            val path: MutableList<HashMap<String, String>> = ArrayList()
            /** Traversing all legs  */
            for (j in 0 until jLegs.length()) {
                jSteps = (jLegs[j] as JSONObject).getJSONArray("steps")
                /** Traversing all steps  */
                for (k in 0 until jSteps.length()) {
                    var polyline = ""
                    polyline =
                        ((jSteps[k] as JSONObject)["polyline"] as JSONObject)["points"] as String
                    val list = decodePoly(polyline)
                    /** Traversing all points  */
                    for (l in list!!.indices) {
                        val hm = HashMap<String, String>()
                        hm["lat"] = java.lang.Double.toString(list[l].latitude)
                        hm["lng"] = java.lang.Double.toString(list[l].longitude)
                        path.add(hm)
                    }
                }
                PATH.routes.add(path)
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    } catch (e: Exception) {
    }
    return PATH.routes
}


    }
}
    private fun decodePoly(encoded: String): List<LatLng>? {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }


