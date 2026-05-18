package com.example.coffeelab_app2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// screen that shows your review history in a calendar style
class ArchiveActivity : AppCompatActivity() {

    // declared up here so all functions in this class can use them
    private lateinit var calendarRow: LinearLayout
    private lateinit var reviewsList: LinearLayout
    private lateinit var monthText: TextView
    private lateinit var reviews: JSONArray

    // these format dates into different readable strings depending on what we need
    private val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())      // e.g. "Mon"
    private val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault())     // e.g. "07"
    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())   // e.g. "May 2025"
    private val compareFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // used to match dates
    private val fullDateFormat = SimpleDateFormat("EEE, MMM dd • h:mm a", Locale.getDefault()) // e.g. "Mon, May 07 • 3:45 PM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        calendarRow = findViewById(R.id.calendarRow)
        reviewsList = findViewById(R.id.reviewsList)
        monthText = findViewById(R.id.monthText)

        // SharedPreferences is how android saves small data locally — we load the saved reviews here
        val prefs = getSharedPreferences("reviews", MODE_PRIVATE)
        reviews = JSONArray(prefs.getString("savedReviewsJson", "[]")) // default to empty array if nothing saved

        // show the current month at the top e.g. "MAY 2025"
        monthText.text = monthFormat.format(Date()).uppercase()
        monthText.setTypeface(null, Typeface.BOLD)

        buildCalendar()
        showReviewsForDate(compareFormat.format(Date())) // show todays reviews on load

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.homeNav).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.discoverNav).setOnClickListener {
            startActivity(Intent(this, allproducts::class.java))
        }

        findViewById<LinearLayout>(R.id.settingsNav).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // builds the row of 7 day cards shown at the top
    private fun buildCalendar() {
        calendarRow.removeAllViews()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -3) // start 3 days ago so today sits in the middle

        repeat(7) {
            val date = calendar.time
            val dateKey = compareFormat.format(date) // "yyyy-MM-dd" string used to match reviews to this day

            // each day is a vertical card built entirely in code
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = roundedBg("#3A241F", 18f)
                layoutParams = LinearLayout.LayoutParams(dp(72), dp(120)).apply {
                    marginEnd = dp(10)
                }
            }

            // short day name at the top of the card e.g. "Mon"
            val dayName = TextView(this).apply {
                text = dayNameFormat.format(date)
                textColor("#E8D7CE")
                textSize = 13f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }

            // day number below the name e.g. "07"
            val dayNumber = TextView(this).apply {
                text = dayNumberFormat.format(date)
                textColor("#E8D7CE")
                textSize = 13f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }

            // shows the drink image if a review exists for this day
            val image = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply {
                    topMargin = dp(10)
                }
                scaleType = ImageView.ScaleType.FIT_CENTER

                val reviewImage = findFirstImageForDate(dateKey)

                // only show the image if we actually found one
                if (reviewImage != null && reviewImage != 0) {
                    setImageResource(reviewImage)
                    visibility = View.VISIBLE
                } else {
                    setImageResource(0)
                    visibility = View.GONE
                }
            }

            item.addView(dayName)
            item.addView(dayNumber)
            item.addView(image)

            // tapping a day loads the reviews for that date
            item.setOnClickListener {
                showReviewsForDate(dateKey)
            }

            calendarRow.addView(item)
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move to the next day before repeating
        }
    }

    // filters all saved reviews and only shows the ones that match the selected date
    private fun showReviewsForDate(dateKey: String) {
        reviewsList.removeAllViews()
        var found = false

        for (i in 0 until reviews.length()) {
            val review = reviews.getJSONObject(i)
            val timestamp = review.getLong("timestamp")
            val reviewDateKey = compareFormat.format(Date(timestamp)) // convert timestamp to "yyyy-MM-dd" to compare

            if (reviewDateKey == dateKey) {
                found = true
                reviewsList.addView(createReviewCard(review))
            }
        }

        // if no reviews matched show a message instead
        if (!found) {
            reviewsList.addView(TextView(this).apply {
                text = "No reviews for this day."
                textColor("#E8D7CE")
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, dp(40), 0, dp(40))
                setTypeface(null, Typeface.BOLD)
            })
        }
    }

    // builds and returns the visual card for a single review
    private fun createReviewCard(review: JSONObject): LinearLayout {

        // optString/optInt safely read from JSON — if the key doesnt exist they return the default instead of crashing
        val productName = review.optString("productName", "Coffee")
        val store = review.optString("store", "Not entered")
        val rating = review.optInt("rating", 0).coerceIn(0, 5) // coerceIn makes sure the value stays between 0 and 5
        val reviewText = review.optString("reviewText", "")
        val productImage = review.optInt("productImage", 0)
        val timestamp = review.optLong("timestamp", System.currentTimeMillis())

        // the outer card with padding and a rounded border
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = roundedStrokeBg("#2F1C18", "#4B342D", 24f, 1)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(18)
            }
        }

        // top row holds the drink image on the left and name + stars on the right
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val img = ImageView(this).apply {
            if (productImage != 0) {
                setImageResource(productImage)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
            layoutParams = LinearLayout.LayoutParams(dp(74), dp(74))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // stacks the product name and star rating vertically next to the image
        // weight = 1f makes it stretch to fill the remaining horizontal space
        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dp(14)
            }
        }

        val title = TextView(this).apply {
            text = productName
            textColor("#F6E5C2")
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
        }

        // builds the star display based on the rating
        val stars = TextView(this).apply {
            text = "★".repeat(rating) + "☆".repeat(5 - rating)
            textColor("#D8A46B")
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }

        titleBox.addView(title)
        titleBox.addView(stars)
        topRow.addView(img)
        topRow.addView(titleBox)

        // shows the numeric rating and cafe name
        val info = TextView(this).apply {
            text = "Overall Rating: $rating\nCafé:\n$store"
            textColor("#E8D7CE")
            textSize = 15f
            setPadding(0, dp(14), 0, 0)
            setTypeface(null, Typeface.BOLD)
        }

        // the written review / shows a placeholder if they didnt write anything
        val body = TextView(this).apply {
            text = if (reviewText.isBlank()) "\"No written review.\"" else "\"$reviewText\""
            textColor("#A58D83")
            textSize = 15f
            setPadding(0, dp(12), 0, 0)
            setTypeface(null, Typeface.BOLD)
        }

        // the date and time the review was submitted
        val dateText = TextView(this).apply {
            text = fullDateFormat.format(Date(timestamp))
            textColor("#D8A46B")
            textSize = 13f
            setPadding(0, dp(18), 0, 0)
            setTypeface(null, Typeface.BOLD)
        }

        card.addView(topRow)
        card.addView(info)
        card.addView(body)
        card.addView(dateText)

        return card
    }

    // loops through all reviews and returns the image from the first one found on that date
    private fun findFirstImageForDate(dateKey: String): Int? {
        for (i in 0 until reviews.length()) {
            val review = reviews.getJSONObject(i)
            val timestamp = review.getLong("timestamp")
            val reviewDateKey = compareFormat.format(Date(timestamp))

            if (reviewDateKey == dateKey) {
                return review.optInt("productImage", 0)
            }
        }
        return null
    // no review found for that date
    }

    private fun TextView.textColor(color: String) {
        setTextColor(Color.parseColor(color))
    }

    // creates a plain rounded background shape with a fill color
    private fun roundedBg(color: String, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = radius * resources.displayMetrics.density
        // convert dp to pixels
        }
    }

    // same as roundedBg but also adds a visible border / stroke around the shape
    private fun roundedStrokeBg(
        fillColor: String,
        strokeColor: String,
        radius: Float,
        strokeWidth: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(fillColor))
            cornerRadius = radius * resources.displayMetrics.density
            setStroke(
                (strokeWidth * resources.displayMetrics.density).toInt(),
                Color.parseColor(strokeColor)
            )
        }
    }

    // helper that converts dp to pixels / needed because android screens have different densities
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}