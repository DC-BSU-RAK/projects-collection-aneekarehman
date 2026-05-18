package com.example.coffeelab_app2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

// this is the first screen that appears when the app opens — it just shows a logo/splash for a moment
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.splashscreen)

        // wait 2.5 seconds then automatically move to the intro screen
        // postDelayed runs the code inside { } after the given delay in milliseconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, IntroActivity::class.java))
            finish() // close the splash screen so the user cant press back to it
        }, 2500)
    }
}