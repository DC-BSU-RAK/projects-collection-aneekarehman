package com.example.apollolit_app1

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

// this is the instructions screen / it just displays a static layout and lets the user close it
class popupinstructions : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popupinstructions)

        // close button just finishes the activity and goes back to the previous screen
        findViewById<ImageButton>(R.id.btnCloseInstructionsPage).setOnClickListener {
            finish()
        }
    }
}