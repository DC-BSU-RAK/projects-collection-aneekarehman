package com.example.apollolit_app1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

// this is the splash screen / it plays a short video and then automatically moves to the main screen
class splashscreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)

        val splashVideo = findViewById<VideoView>(R.id.splashVideo)

        // Uri.parse builds a path to the video file stored in the res/raw folder
        val uri = Uri.parse("android.resource://$packageName/${R.raw.splash_video}")
        splashVideo.setVideoURI(uri)

        // when the video finishes playing, move to the main screen and close this one
        splashVideo.setOnCompletionListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        splashVideo.start()
    }
}