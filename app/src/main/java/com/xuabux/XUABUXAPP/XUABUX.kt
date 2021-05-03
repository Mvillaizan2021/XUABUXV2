package com.xuabux.XUABUXAPP

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity

class XUABUX : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = applicationContext
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {

                val registro = Intent(this@XUABUX, LOGIN::class.java)
                registro.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                registro.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK );
                registro.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(registro)
            }
        }.start()
    }
}