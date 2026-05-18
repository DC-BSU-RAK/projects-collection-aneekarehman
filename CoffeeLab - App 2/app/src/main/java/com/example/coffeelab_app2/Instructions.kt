package com.example.coffeelab_app2

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Instructions : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}

// For the instructions page, this is a simple background image