package com.example.coffeelab_app2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

// this is the intro / splash screen that shows a slideshow of images before the user signs in
class IntroActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    // Handler lets us schedule code to run after a delay — we use it to auto-slide the images
    // Looper.getMainLooper() makes sure it runs on the main UI thread
    private val handler = Handler(Looper.getMainLooper())

    // this is the auto-slide logic — it runs every 3 seconds and moves to the next image
    // "object : Runnable" means we're creating a one-off Runnable right here instead of a separate class
    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            val adapter = viewPager.adapter ?: return // if theres no adapter yet, do nothing
            val nextItem = (viewPager.currentItem + 1) % adapter.itemCount // % wraps back to 0 after the last slide
            viewPager.setCurrentItem(nextItem, true) // true = animate the slide
            handler.postDelayed(this, 3000) // schedule itself to run again in 3 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.introactivity)

        viewPager = findViewById(R.id.introViewPager)

        // the list of images that will be shown in the slideshow
        val images = listOf(
            R.drawable.first,
            R.drawable.second,
            R.drawable.third,
            R.drawable.fourth
        )

        viewPager.adapter = IntroAdapter(images)

        // this customises how the page transition looks when sliding between images
        // "position" is 0 when the page is fully in view, and moves toward -1 or 1 as it slides away
        viewPager.setPageTransformer { page, position ->
            page.alpha = 1 - kotlin.math.abs(position) // fades the page out as it slides away
            page.translationX = -position * page.width // cancels the default slide movement
            page.scaleX = 0.95f + (1 - kotlin.math.abs(position)) * 0.05f // slightly shrinks pages that arent active
            page.scaleY = 0.95f + (1 - kotlin.math.abs(position)) * 0.05f
        }

        // the dot indicators at the bottom that show which slide youre on
        val dotsIndicator = findViewById<WormDotsIndicator>(R.id.dotsIndicator)
        dotsIndicator.attachTo(viewPager) // links the dots to the viewpager so they update automatically

        // load and start the pulse animation on the button to grab the users attention
        val discoverButton = findViewById<Button>(R.id.btnDiscoverMore)
        val pulse = AnimationUtils.loadAnimation(this, R.anim.button_pulse)
        discoverButton.startAnimation(pulse)

        // tapping the button takes you to the sign in screen and closes this one
        discoverButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        // start the auto-slide after 5 seconds — gives the user a moment to read before it starts moving
        handler.postDelayed(autoSlideRunnable, 5000)
    }

    // onDestroy is called when the screen closes
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoSlideRunnable)
    }
}