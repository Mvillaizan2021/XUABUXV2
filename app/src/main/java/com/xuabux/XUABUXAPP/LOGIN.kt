package com.xuabux.XUABUXAPP

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LOGIN : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_l_o_g_i_n)
        mAuth = FirebaseAuth.getInstance()
       
        }

    fun onRegistroIBoton(view: View) {
        val RegistroIntent = Intent(this, REGISTRO::class.java)
        startActivity(RegistroIntent)
    }

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
                val mapaIntent = Intent(this, MAPA::class.java)
                startActivity(mapaIntent)

            }
            else{
                Toast.makeText(this@LOGIN, "Error de loggeo", Toast.LENGTH_LONG).show()

            }
        }

    }


}



