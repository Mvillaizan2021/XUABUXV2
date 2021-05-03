package com.xuabux.XUABUXAPP

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LOGIN : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_l_o_g_i_n)


       
        }

    fun onRegistroIBoton(view: View) {
        val mapaI = Intent(this, REGISTRO::class.java)
        startActivity(mapaI)
    }


}



