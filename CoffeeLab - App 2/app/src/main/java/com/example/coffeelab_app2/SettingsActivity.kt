package com.example.coffeelab_app2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

// this is the settings screen where the user can manage notifications, clear history, and export reviews
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // load the settings that were previously saved — "settings" is the name of this preference file
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val reviewReminderSwitch = findViewById<Switch>(R.id.reviewReminderSwitch)
        val newFeaturesSwitch = findViewById<Switch>(R.id.newFeaturesSwitch)
        val dealsSwitch = findViewById<Switch>(R.id.dealsSwitch)
        val nearbySwitch = findViewById<Switch>(R.id.nearbySwitch)
        val autoSaveSwitch = findViewById<Switch>(R.id.autoSaveSwitch)

        // set each switch to whatever was saved — defaults to true if nothing was saved yet
        reviewReminderSwitch.isChecked = prefs.getBoolean("reviewReminder", true)
        newFeaturesSwitch.isChecked = prefs.getBoolean("newFeatures", true)
        dealsSwitch.isChecked = prefs.getBoolean("deals", true)
        nearbySwitch.isChecked = prefs.getBoolean("nearby", true)
        autoSaveSwitch.isChecked = prefs.getBoolean("autoSave", true)

        // save all switch states when the user taps save
        findViewById<Button>(R.id.saveChangesButton).setOnClickListener {
            prefs.edit()
                .putBoolean("reviewReminder", reviewReminderSwitch.isChecked)
                .putBoolean("newFeatures", newFeaturesSwitch.isChecked)
                .putBoolean("deals", dealsSwitch.isChecked)
                .putBoolean("nearby", nearbySwitch.isChecked)
                .putBoolean("autoSave", autoSaveSwitch.isChecked)
                .apply() // apply() saves in the background without blocking the UI

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }

        // wipes all saved reviews from SharedPreferences
        findViewById<TextView>(R.id.clearHistoryButton).setOnClickListener {
            getSharedPreferences("reviews", MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            Toast.makeText(this, "Review history cleared", Toast.LENGTH_SHORT).show()
        }

        // lets the user share their saved reviews as plain text via any app on their phone
        findViewById<TextView>(R.id.exportReviewsButton).setOnClickListener {
            val reviews = getSharedPreferences("reviews", MODE_PRIVATE)
                .getString("savedReviewsJson", "[]")

            // ACTION_SEND opens androids built in share sheet so the user can pick where to send it
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "CoffeeLab Reviews")
                putExtra(Intent.EXTRA_TEXT, reviews)
            }

            // createChooser wraps the intent so android shows the app picker instead of opening one automatically
            startActivity(Intent.createChooser(shareIntent, "Export Reviews"))
        }

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.backHomeText).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // bottom navigation
        findViewById<LinearLayout>(R.id.homeNav).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.discoverNav).setOnClickListener {
            startActivity(Intent(this, allproducts::class.java))
        }

        findViewById<LinearLayout>(R.id.activityNav).setOnClickListener {
            startActivity(Intent(this, ArchiveActivity::class.java))
        }

        // we're already on this screen so just show a message instead of reloading it
        findViewById<LinearLayout>(R.id.settingsNav).setOnClickListener {
            Toast.makeText(this, "You are already in Settings", Toast.LENGTH_SHORT).show()
        }
    }
}