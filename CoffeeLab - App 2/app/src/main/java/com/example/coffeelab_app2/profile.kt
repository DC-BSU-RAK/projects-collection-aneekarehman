package com.example.coffeelab_app2

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// this screen shows the users saved profile information
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // load the user data that was saved during sign up
        val prefs = getSharedPreferences("user", MODE_PRIVATE)

        // fill each text field with the saved value — "Not saved" shows if nothing was found
        findViewById<TextView>(R.id.firstNameText).text =
            prefs.getString("firstName", "Not saved")

        findViewById<TextView>(R.id.lastNameText).text =
            prefs.getString("lastName", "Not saved")

        findViewById<TextView>(R.id.emailText).text =
            prefs.getString("email", "Not saved")

        findViewById<TextView>(R.id.passwordText).text =
            prefs.getString("password", "Not saved")

        // back button just closes this screen and goes back to the previous one
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}