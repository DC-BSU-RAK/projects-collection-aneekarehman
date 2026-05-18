package com.example.apollolit_app1

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlin.math.abs

// this is the main screen of the app — it handles genre/length selection, animations, video background and music
class MainActivity : AppCompatActivity() {

    // ExoPlayer handles the looping video background, MediaPlayer handles the background music
    private var player: ExoPlayer? = null
    private var mediaPlayer: MediaPlayer? = null

    // tracks which genre and length the user has currently selected
    private var selectedGenre: String? = null
    private var selectedLength: String? = null

    // keeps a reference to the currently highlighted book so we can deselect it when the user picks a new one
    private var selectedGenreView: ImageView? = null
    private var selectedLengthView: ImageView? = null

    private lateinit var speechBubble: TextView
    private lateinit var bookPopup: FrameLayout
    private lateinit var musicPopup: FrameLayout
    private lateinit var popupImageView: ImageView
    private lateinit var btnFindStory: ImageButton
    private lateinit var btnClear: ImageButton

    // these animate the find story and clear buttons with a floating up and down effect
    private var findStoryAnimator: ObjectAnimator? = null
    private var clearBtnAnimator: ObjectAnimator? = null

    // stores preloaded drawables so we dont have to reload them every time a popup opens
    private val popupDrawables = mutableMapOf<Int, Drawable?>()

    // maps every genre + length combination to its matching popup image
    // "lazy" means this map is only built the first time it's accessed, not when the class is created
    private val popupMap: Map<String, Int> by lazy {
        mapOf(
            "fantasy_brief" to R.drawable.popup_fantasy_brief,
            "fantasy_regular" to R.drawable.popup_fantasy_regular,
            "fantasy_comprehensive" to R.drawable.popup_fantasy_comprehensive,
            "scifi_brief" to R.drawable.popup_scifi_brief,
            "scifi_regular" to R.drawable.popup_scifi_regular,
            "scifi_comprehensive" to R.drawable.popup_scifi_comprehensive,
            "mystery_brief" to R.drawable.popup_mystery_brief,
            "mystery_regular" to R.drawable.popup_mystery_regular,
            "mystery_comprehensive" to R.drawable.popup_mystery_comprehensive,
            "horror_brief" to R.drawable.popup_horror_brief,
            "horror_regular" to R.drawable.popup_horror_regular,
            "horror_comprehensive" to R.drawable.popup_horror_comprehensive,
            "history_brief" to R.drawable.popup_history_brief,
            "history_regular" to R.drawable.popup_history_regular,
            "history_comprehensive" to R.drawable.popup_history_comprehensive,
            "romance_brief" to R.drawable.popup_romance_brief,
            "romance_regular" to R.drawable.popup_romance_regular,
            "romance_comprehensive" to R.drawable.popup_romance_comprehensive
        )
    }

    // used to schedule and cancel the typewriter animation
    private val typewriterHandler = Handler(Looper.getMainLooper())
    private var typewriterRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // makes the app draw behind the system bars for a full screen look
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_main)

        speechBubble = findViewById(R.id.tvSpeechBubble)
        bookPopup = findViewById(R.id.bookPopup)
        musicPopup = findViewById(R.id.musicPopup)
        popupImageView = findViewById(R.id.popupImage)
        btnFindStory = findViewById(R.id.btnFindStory)
        btnClear = findViewById(R.id.btnClear)

        // add a press scale animation to these buttons so they feel responsive when tapped
        addPressAnimation(findViewById(R.id.btnMusic))
        addPressAnimation(findViewById(R.id.btnInfo))
        addPressAnimation(btnFindStory)
        addPressAnimation(btnClear)

        // hardware layer makes animations smoother by rendering these buttons on the GPU
        btnFindStory.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        btnClear.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        preloadPopupImages()
        startAnimations()

        // VIDEO BACKGROUND
        // ExoPlayer is androids modern media player — we use it here just for a silent looping background video
        player = ExoPlayer.Builder(this).build()

        findViewById<PlayerView>(R.id.videoBackground).player = player

        val mediaItem =
            MediaItem.fromUri("android.resource://$packageName/${R.raw.background}")

        player?.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE // loop forever
            volume = 0f // muted — its just visual
            prepare()
            play()
        }

        // MUSIC
        // MediaPlayer handles the background music separately from the video
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)

        mediaPlayer?.apply {
            isLooping = true
            setVolume(1.0f, 1.0f)
            start()
        }

        // show the welcome message with a typewriter effect when the screen loads
        typewriterAnimate(
            speechBubble,
            "Welcome, Reader! Choose your genre from the shelves..."
        )

        setupPopupCloseButtons()
        setupMusicSeekBar()
        setupMainButtons()
        setupGenreBooks()
        setupLengthBooks()
    }

    // loads all popup images into memory upfront so there's no delay when showing them
    private fun preloadPopupImages() {
        popupMap.values.distinct().forEach { resId ->
            popupDrawables[resId] =
                AppCompatResources.getDrawable(this, resId)
        }
    }

    // starts the floating animations on the find story and clear buttons
    // the null check makes sure we dont accidentally start them twice
    private fun startAnimations() {

        if (findStoryAnimator == null) {

            findStoryAnimator =
                createFloatingAnimator(btnFindStory, 0)

            // 300ms delay so the two buttons float slightly out of sync with each other
            clearBtnAnimator =
                createFloatingAnimator(btnClear, 300)

            findStoryAnimator?.start()
            clearBtnAnimator?.start()
        }
    }

    // creates a looping up-down float animation on a view
    // TRANSLATION_Y moves the view vertically — 0f to -10f means it floats 10 pixels upward
    private fun createFloatingAnimator(
        view: View,
        delay: Long
    ): ObjectAnimator {

        return ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            0f,
            -10f
        ).apply {

            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE // goes up then comes back down
            interpolator = AccelerateDecelerateInterpolator() // eases in and out smoothly
            startDelay = delay
        }
    }

    private fun setupMainButtons() {

        // info button opens the instructions screen
        findViewById<ImageButton>(R.id.btnInfo)
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        popupinstructions::class.java
                    )
                )
            }

        // music button fades in the music volume popup
        findViewById<ImageButton>(R.id.btnMusic)
            .setOnClickListener {

                musicPopup.alpha = 0f
                musicPopup.visibility = View.VISIBLE

                musicPopup.animate()
                    .alpha(1f)
                    .setDuration(90)
                    .start()
            }

        // find story button checks both selections are made before showing the popup
        btnFindStory.setOnClickListener {

            val genre = selectedGenre
            val length = selectedLength

            if (genre == null) {

                typewriterAnimate(
                    speechBubble,
                    "You must choose a genre before I can find your story!"
                )

            } else if (length == null) {

                typewriterAnimate(
                    speechBubble,
                    "Almost there! Select a length from the bottom shelf..."
                )

            } else {

                // combine genre and length into a key like "fantasy_brief" to look up the right popup image
                val key = "${genre}_${length}"

                popupMap[key]?.let {
                    showBookPopup(it)
                }
            }
        }

        // clear button resets all selections and updates the speech bubble
        btnClear.setOnClickListener {

            selectedGenreView?.let {
                deselectBook(it)
            }

            selectedLengthView?.let {
                deselectBook(it)
            }

            selectedGenre = null
            selectedLength = null
            selectedGenreView = null
            selectedLengthView = null

            typewriterAnimate(
                speechBubble,
                "Choices cleared! Choose your genre from the shelves..."
            )
        }
    }

    // sets up click listeners for all six genre book images
    private fun setupGenreBooks() {

        val genres = mapOf(
            "fantasy" to R.id.bookFantasy,
            "scifi" to R.id.bookScifi,
            "mystery" to R.id.bookMystery,
            "horror" to R.id.bookHorror,
            "history" to R.id.bookHistory,
            "romance" to R.id.bookRomance
        )

        genres.forEach { (genre, id) ->

            findViewById<ImageView>(id)
                .setOnClickListener {

                    // if the user taps the already selected genre do nothing
                    if (selectedGenre == genre)
                        return@setOnClickListener

                    selectedGenre = genre

                    // selectBook highlights the new one and deselects the previous one
                    selectedGenreView =
                        selectBook(
                            it as ImageView,
                            selectedGenreView
                        )

                    typewriterAnimate(
                        speechBubble,
                        "Excellent choice! Now select your preferred length below..."
                    )
                }
        }
    }

    // sets up click listeners for the three length book images
    private fun setupLengthBooks() {

        val lengths = mapOf(
            "brief" to R.id.bookBrief,
            "regular" to R.id.bookRegular,
            "comprehensive" to R.id.bookComprehensive
        )

        lengths.forEach { (length, id) ->

            findViewById<ImageView>(id)
                .setOnClickListener {

                    // dont let the user pick a length before picking a genre
                    if (selectedGenre == null) {

                        typewriterAnimate(
                            speechBubble,
                            "Hold on! You must choose a genre first..."
                        )

                        return@setOnClickListener
                    }

                    // if the user taps the already selected length do nothing
                    if (selectedLength == length)
                        return@setOnClickListener

                    selectedLength = length

                    selectedLengthView =
                        selectBook(
                            it as ImageView,
                            selectedLengthView
                        )

                    typewriterAnimate(
                        speechBubble,
                        "Perfect! Now press Find My Story to begin!"
                    )
                }
        }
    }

    private fun setupPopupCloseButtons() {

        findViewById<ImageButton>(R.id.btnClosePopup)
            .setOnClickListener {
                hidePopup(bookPopup)
            }

        findViewById<ImageButton>(R.id.btnCloseMusic)
            .setOnClickListener {
                hidePopup(musicPopup)
            }
    }

    // shows the book popup by setting the preloaded image and fading it in
    private fun showBookPopup(resId: Int) {

        popupImageView.setImageDrawable(
            popupDrawables[resId]
        )

        bookPopup.animate().cancel() // cancel any ongoing animation before starting a new one

        bookPopup.alpha = 0f
        bookPopup.visibility = View.VISIBLE

        bookPopup.animate()
            .alpha(1f)
            .setDuration(90)
            .start()
    }

    // fades out a popup then hides it — withEndAction runs after the animation finishes
    private fun hidePopup(popup: View) {

        popup.animate().cancel()

        popup.animate()
            .alpha(0f)
            .setDuration(80)
            .withEndAction {

                popup.visibility = View.GONE
                popup.alpha = 1f // reset alpha so it fades in correctly next time
            }
            .start()
    }

    private fun setupMusicSeekBar() {

        val seekBar =
            findViewById<SeekBar>(R.id.seekBarVolume)

        seekBar.progress = 75

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                private var lastVol = -1f

                override fun onProgressChanged(
                    sb: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    if (fromUser) {

                        // squaring the value makes the volume curve feel more natural to the human ear
                        val vol =
                            (progress / 100f) *
                                    (progress / 100f)

                        // only update if the change is noticeable — avoids unnecessary calls
                        if (abs(vol - lastVol) > 0.005f) {

                            mediaPlayer?.setVolume(vol, vol)
                            lastVol = vol
                        }
                    }
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}

                override fun onStopTrackingTouch(sb: SeekBar?) {}
            }
        )
    }

    // animates text appearing word by word instead of letter by letter for better performance
    // cancels any previous typewriter animation before starting a new one
    private fun typewriterAnimate(
        textView: TextView,
        text: String
    ) {

        typewriterRunnable?.let {
            typewriterHandler.removeCallbacks(it)
        }

        textView.text = ""

        val words = text.split(" ")

        var wordIndex = 0

        val wordsPerTick = 5 // how many words to add each tick

        typewriterRunnable = object : Runnable {

            override fun run() {

                if (wordIndex < words.size) {

                    // coerceAtMost makes sure we dont go past the end of the word list
                    wordIndex =
                        (wordIndex + wordsPerTick)
                            .coerceAtMost(words.size)

                    // take the words up to the current index and join them back into a string
                    textView.text =
                        words.take(wordIndex)
                            .joinToString(" ")

                    typewriterHandler.postDelayed(
                        this,
                        70L
                    )

                } else {

                    // make sure the final text is exactly right and clear the runnable reference
                    textView.text = text
                    typewriterRunnable = null
                }
            }
        }

        typewriterHandler.post(typewriterRunnable!!)
    }

    // highlights a newly selected book and deselects the previous one
    private fun selectBook(
        newView: ImageView,
        previousView: ImageView?
    ): ImageView {

        if (newView == previousView)
            return newView

        previousView?.let {
            deselectBook(it)
        }

        newView.animate().cancel()

        newView.alpha = 1f

        // hardware layer makes the scale animation smoother
        newView.setLayerType(
            View.LAYER_TYPE_HARDWARE,
            null
        )

        // length books get a slightly bigger scale since they're smaller images
        val isLengthBook =
            newView.id == R.id.bookBrief ||
                    newView.id == R.id.bookRegular ||
                    newView.id == R.id.bookComprehensive

        // add a border overlay to show the book is selected
        newView.foreground =
            AppCompatResources.getDrawable(
                this,
                R.drawable.selected_book_border
            )

        val scaleAmount =
            if (isLengthBook) 1.08f else 1.07f

        newView.animate()
            .scaleX(scaleAmount)
            .scaleY(scaleAmount)
            .alpha(0.92f)
            .setDuration(120)
            .start()

        return newView
    }

    // animates the book back to its original size and removes the selection border
    private fun deselectBook(view: ImageView) {

        view.animate().cancel()

        view.foreground = null

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(120)
            .withEndAction {

                // switch back to no layer type once the animation is done to save memory
                view.setLayerType(
                    View.LAYER_TYPE_NONE,
                    null
                )
            }
            .start()
    }

    // adds a scale up on press and scale back on release to make buttons feel physical
    private fun addPressAnimation(button: View) {

        button.setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    v.animate()
                        .scaleX(1.12f)
                        .scaleY(1.12f)
                        .setDuration(90)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(90)
                        .start()
                }
            }

            // return false so the normal click listener still fires after the touch animation
            false
        }
    }

    // pause both the music and video when the user leaves the screen
    override fun onPause() {
        super.onPause()

        mediaPlayer?.pause()
        player?.pause()
    }

    // resume playback when the user comes back to the screen
    override fun onResume() {
        super.onResume()

        // recreate the MediaPlayer if it was released — this can happen if the app was in the background too long
        if (mediaPlayer == null) {

            mediaPlayer =
                MediaPlayer.create(
                    this,
                    R.raw.background_music
                )

            mediaPlayer?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
            }
        }

        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }

        player?.play()
    }

    // clean up everything when the screen is destroyed to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()

        // cancel the typewriter so it doesnt try to update a view that no longer exists
        typewriterRunnable?.let {
            typewriterHandler.removeCallbacks(it)
        }

        findStoryAnimator?.cancel()
        clearBtnAnimator?.cancel()

        // release() frees the memory used by the players — important to always do this
        player?.release()
        mediaPlayer?.release()

        player = null
        mediaPlayer = null
    }
}
