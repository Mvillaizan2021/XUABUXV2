package com.xuabux.XUABUXAPP

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import maes.tech.intentanim.CustomIntent
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap


class LOGIN : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Llamamos la integracion de FireBase
        mAuth = FirebaseAuth.getInstance()
        Log.d("Xuadebug","Cargado");

    }
//no tienes cuenta?, aqui esta el boton que te llevara a la ventana para el registro
    fun onRegistroIBoton(view: View) {
        val RegistroIntent = Intent(this, REGISTRO::class.java)
        startActivity(RegistroIntent)
        CustomIntent.customType(this, "fadein-to-fadeout")
    }
//comparamos los datos ingresado con los dispuestos en la base de datos para dar acceso
    fun LoginBoton(view: View) {
        var usuario: EditText? = null
        usuario = findViewById(R.id.EmailLoginET)
        var pass: EditText? = null
        pass = findViewById(R.id.ContraseñaET)
        val user:String = usuario.text.toString();
        val password:String = pass.text.toString();
        if (user.isEmpty()) {
            usuario!!.error = "No hay Email"
            usuario!!.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
            usuario!!.error = "Ingrese un email valido"
            usuario!!.requestFocus()
            return
        }

        if (password.isEmpty()) {
            pass!!.error = "No hay contraseña"
            pass!!.requestFocus()
            return
        }
        mAuth?.signInWithEmailAndPassword(user,password)?.addOnCompleteListener(this){
            task ->
            if (task.isSuccessful){
                val mapaIntent = Intent(this, Overlay::class.java)
                mapaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mapaIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK );
                mapaIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(mapaIntent)

            }
            else{
                Toast.makeText(this@LOGIN, "Usuario o contraseña incorrecta.", Toast.LENGTH_LONG).show()

            }
        }

    }


    }

var txtLatInicio: EditText? =
    null, var txtLongInicio:EditText? = null, var txtLatFinal:EditText? = null, var txtLongFinal:EditText? = null

var jsonObjectRequest: JsonObjectRequest? = null
var request: RequestQueue? = null

fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(toolbar)
    txtLatInicio = findViewById(R.id.txtLatIni) as EditText?
    txtLongInicio = findViewById(R.id.txtLongIni) as EditText
    txtLatFinal = findViewById(R.id.txtLatFin) as EditText
    txtLongFinal = findViewById(R.id.txtLongFin) as EditText
    val fab: FloatingActionButton = findViewById(R.id.fab) as FloatingActionButton
    fab.setOnClickListener(View.OnClickListener {
        Utilidades.coordenadas.setLatitudInicial(java.lang.Double.valueOf(txtLatInicio!!.text.toString()))
        Utilidades.coordenadas.setLongitudInicial(
            java.lang.Double.valueOf(
                txtLongInicio.getText().toString()
            )
        )
        Utilidades.coordenadas.setLatitudFinal(
            java.lang.Double.valueOf(
                txtLatFinal.getText().toString()
            )
        )
        Utilidades.coordenadas.setLongitudFinal(
            java.lang.Double.valueOf(
                txtLongFinal.getText().toString()
            )
        )
        webServiceObtenerRuta(
            txtLatInicio!!.text.toString(), txtLongInicio.getText().toString(),
            txtLatFinal.getText().toString(), txtLongFinal.getText().toString()
        )
        val miIntent = Intent(this@MainActivity, MapsActivity::class.java)
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
                                val list: List<LatLng> = decodePoly(polyline)
                                /** Traversing all points  */
                                for (l in list.indices) {
                                    val hm = HashMap<String, String>()
                                    hm["lat"] = java.lang.Double.toString(list[l].latitude)
                                    hm["lng"] = java.lang.Double.toString(list[l].longitude)
                                    path.add(hm)
                                }
                            }
                            Utilidades.routes.add(path)
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
                    val list: List<LatLng> = decodePoly(polyline)
                    /** Traversing all points  */
                    for (l in list.indices) {
                        val hm = HashMap<String, String>()
                        hm["lat"] = java.lang.Double.toString(list[l].latitude)
                        hm["lng"] = java.lang.Double.toString(list[l].longitude)
                        path.add(hm)
                    }
                }
                Utilidades.routes.add(path)
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    } catch (e: Exception) {
    }
    return Utilidades.routes
}


fun onClick(view: View) {
    if (view.id == R.id.btnObtenerCoordenadas) {
        txtLatInicio!!.setText("4.543986")
        txtLongInicio.setText("-75.666736")
        //Unicentro
        txtLatFinal.setText("4.540026")
        txtLongFinal.setText("-75.665479")
        //Parque del café
        //  txtLatFinal.setText("4.541396"); txtLongFinal.setText("-75.771741");
    }
}

private fun decodePoly(encoded: String): List<LatLng> {
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

fun onCreateOptionsMenu(menu: Menu?): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu)
    return true
}

fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId
    return if (id == R.id.action_settings) {
        true
    } else super.onOptionsItemSelected(item)
}

}



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

}

}



