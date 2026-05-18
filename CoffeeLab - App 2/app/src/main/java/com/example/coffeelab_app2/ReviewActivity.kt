package com.example.coffeelab_app2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

// this is the screen where the user writes and saves their review for a drink
class ReviewActivity : AppCompatActivity() {

    // tracks the current star rating the user has selected
    private var rating = 0
    private lateinit var stars: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // read all the data passed in from the product detail screen
        val productName = intent.getStringExtra("productName") ?: "Coffee"
        val productCategory = intent.getStringExtra("productCategory") ?: "All Drinks"
        val productImage = intent.getIntExtra("productImage", R.drawable.latte)
        val price = intent.getStringExtra("price") ?: ""
        val store = intent.getStringExtra("store") ?: ""
        val sweetness = intent.getStringExtra("sweetness") ?: "Medium Sweet"

        val productNameText = findViewById<TextView>(R.id.productNameText)
        val productImageView = findViewById<ImageView>(R.id.reviewProductImage)
        val detailsText = findViewById<TextView>(R.id.detailsText)
        val ratingLabel = findViewById<TextView>(R.id.ratingLabel)
        val reviewInput = findViewById<EditText>(R.id.reviewInput)
        val saveReviewButton = findViewById<Button>(R.id.saveReviewButton)

        productNameText.text = productName
        productNameText.setTypeface(null, Typeface.BOLD)

        productImageView.setImageResource(productImage)
        productImageView.translationX = 10f // nudge the image slightly to the right

        // build the summary text from the passed in data
        // ifEmpty() shows "Not entered" if the user left that field blank on the previous screen
        detailsText.text =
            "Purchased From\n${store.ifEmpty { "Not entered" }}\n\n" +
                    "Category\n$productCategory\n\n" +
                    "Price\n${price.ifEmpty { "Not entered" }}\n\n" +
                    "Sweetness\n$sweetness"
        detailsText.setTypeface(null, Typeface.BOLD)

        ratingLabel.setTypeface(null, Typeface.BOLD)
        reviewInput.setTypeface(null, Typeface.BOLD)
        saveReviewButton.setTypeface(null, Typeface.BOLD)

        // collect the 5 star TextViews into a list so we can loop through them easily
        stars = listOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )

        // forEachIndexed gives us both the star and its position (0-4) at the same time
        // we add 1 so tapping the first star sets rating to 1 not 0
        stars.forEachIndexed { index, star ->
            star.setTypeface(null, Typeface.BOLD)
            star.setOnClickListener {
                setRating(index + 1)
            }
        }

        // these are the flavour tag chips the user can toggle on or off
        val chips = listOf(
            findViewById<TextView>(R.id.chocolateChip),
            findViewById<TextView>(R.id.caramelChip),
            findViewById<TextView>(R.id.nuttyChip),
            findViewById<TextView>(R.id.fruityChip),
            findViewById<TextView>(R.id.floralChip),
            findViewById<TextView>(R.id.citrusChip)
        )

        chips.forEach { chip ->
            chip.background = chipBg(false)
            chip.setTextColor(Color.parseColor("#FFF3E5"))
            chip.setTypeface(null, Typeface.BOLD)

            // we use chip.tag to remember if its selected or not — tag is a general purpose field every View has
            chip.setOnClickListener {
                val selected = chip.tag != true
                chip.tag = selected
                chip.background = chipBg(selected)
                chip.setTextColor(
                    Color.parseColor(
                        if (selected) "#2A1713" else "#FFF3E5"
                    )
                )
            }
        }

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        saveReviewButton.setOnClickListener {

            // load whatever reviews are already saved so we can add to them without overwriting
            val prefs = getSharedPreferences("reviews", MODE_PRIVATE)
            val oldData = prefs.getString("savedReviewsJson", "[]")
            val reviewsArray = JSONArray(oldData)

            // build a JSON object with all the review data to store
            val reviewObject = JSONObject().apply {
                put("productName", productName)
                put("productCategory", productCategory)
                put("productImage", productImage)
                put("store", store.ifEmpty { "Not entered" })
                put("price", price.ifEmpty { "Not entered" })
                put("rating", rating)
                put("reviewText", reviewInput.text.toString())
                put("timestamp", System.currentTimeMillis()) // saves the exact time the review was submitted
            }

            // add the new review to the existing array and save it back
            reviewsArray.put(reviewObject)

            prefs.edit()
                .putString("savedReviewsJson", reviewsArray.toString())
                .apply() // apply() saves asynchronously in the background

            Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show()

            // go straight to the archive screen so the user can see their saved review
            startActivity(Intent(this, ArchiveActivity::class.java))
            finish()
        }
    }

    // updates the star display and rating label based on the value the user tapped
    private fun setRating(value: Int) {
        rating = value

        // fill stars up to the selected rating and leave the rest empty
        stars.forEachIndexed { index, star ->
            star.text = if (index < rating) "★" else "☆"
        }

        // show a word that matches the rating number
        findViewById<TextView>(R.id.ratingLabel).text = when (rating) {
            1 -> "Poor"
            2 -> "Okay"
            3 -> "Good"
            4 -> "Great"
            5 -> "Excellent"
            else -> "Tap to rate"
        }
    }

    // returns a rounded background for a chip — highlighted if selected, dark if not
    private fun chipBg(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(if (selected) "#D8A46B" else "#2A1713"))
            cornerRadius = 24f * resources.displayMetrics.density
            setStroke(
                2,
                Color.parseColor(if (selected) "#D8A46B" else "#7A6256")
            )
        }
    }
}