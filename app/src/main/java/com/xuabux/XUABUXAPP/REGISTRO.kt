package com.xuabux.XUABUXAPP

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import maes.tech.intentanim.CustomIntent.customType

class REGISTRO : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var usuario: EditText? = null
    private var contraseña: EditText? = null
    private var contraseña2: EditText? = null
    private var email: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        email = findViewById(R.id.Email)
        usuario = findViewById(R.id.Usuario)
        contraseña = findViewById(R.id.Contraseña)
        contraseña2 = findViewById(R.id.Contraseña2)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.BotonRegistro -> {
                registrar()
            }

        }
    }
    fun volverboton(v: View) {
        val VolverIntent = Intent(this, LOGIN::class.java)

          VolverIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       // VolverIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(VolverIntent)
        customType(this,"fadein-to-fadeout")
    }

    fun registrar() {
        val NUsuario = usuario!!.text.toString().trim { it <= ' ' }
        val ContraseñaS = contraseña!!.text.toString().trim { it <= ' ' }
        val Contraseña2S = contraseña2!!.text.toString().trim { it <= ' ' }
        val Email = email!!.text.toString().trim { it <= ' ' }
        if (Email.isEmpty()) {
            email!!.error = "No hay Email"
            email!!.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            email!!.error = "Ingrese un email valido"
            email!!.requestFocus()
            return
        }
        if (NUsuario.isEmpty()) {
            usuario!!.error = "No hay usuario"
            usuario!!.requestFocus()
            return
        }
        if (ContraseñaS.isEmpty()) {
            contraseña!!.error = "No hay contraseña"
            contraseña!!.requestFocus()
            return
        }
        if (ContraseñaS.length < 6) {
            contraseña!!.error = "La contraseña es muy corta"
            contraseña!!.requestFocus()
            return
        }
        if (Contraseña2S.isEmpty()) {
            contraseña2!!.error = "No hay contraseña"
            contraseña2!!.requestFocus()
            return
        }
        if (Contraseña2S != ContraseñaS) {
            contraseña2!!.error = "Las contraseñas no coinciden"
            contraseña2!!.requestFocus()
            return
        }
        mAuth!!.createUserWithEmailAndPassword(Email, ContraseñaS)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val usuario = Usuario(NUsuario, Email)
                        FirebaseDatabase.getInstance().getReference("Usuarios")
                                .child(FirebaseAuth.getInstance().currentUser.uid).setValue(NUsuario).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@REGISTRO, "Registrado Correctamente", Toast.LENGTH_LONG)
                                    } else {
                                        Toast.makeText(this@REGISTRO, "Error de registro", Toast.LENGTH_LONG)
                                    }
                                }
                    } else {
                        Toast.makeText(this@REGISTRO, "Error de registro", Toast.LENGTH_LONG)
                    }
                }
    }
}