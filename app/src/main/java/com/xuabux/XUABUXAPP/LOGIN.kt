package com.xuabux.XUABUXAPP

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import maes.tech.intentanim.CustomIntent

class LOGIN : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
       
        }

    fun onRegistroIBoton(view: View) {
        val RegistroIntent = Intent(this, REGISTRO::class.java)
        startActivity(RegistroIntent)
        CustomIntent.customType(this, "fadein-to-fadeout")
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



