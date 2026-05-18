package com.example.coffeelab_app2

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// this is the sign up screen — the user fills in their details here before entering the app
class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin)

        val firstNameInput = findViewById<EditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<EditText>(R.id.lastNameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        // FONT
        firstNameInput.setTypeface(null, Typeface.BOLD)
        lastNameInput.setTypeface(null, Typeface.BOLD)
        emailInput.setTypeface(null, Typeface.BOLD)
        passwordInput.setTypeface(null, Typeface.BOLD)
        signUpButton.setTypeface(null, Typeface.BOLD)

        // INPUT BACKGROUNDS
        val inputBg = roundedBg("#FFF8F1", 18f)

        // we create one background and use newDrawable() to make copies for the others
        // you cant set the same drawable object on multiple views — each needs its own instance
        firstNameInput.background = inputBg
        lastNameInput.background = inputBg.constantState?.newDrawable()
        emailInput.background = inputBg.constantState?.newDrawable()
        passwordInput.background = inputBg.constantState?.newDrawable()

        // FORCE BEIGE BUTTON
        // Button tint has to be set this way otherwise android overrides the color with its default theme
        signUpButton.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#D8A46B"))

        signUpButton.setTextColor(Color.parseColor("#4A1F12"))

        // BUTTON CLICK
        signUpButton.setOnClickListener {

            // trim() removes any accidental spaces the user might have typed at the start or end
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // check all fields are filled before doing anything
            if (
                firstName.isEmpty() ||
                lastName.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // SAVE USER DATA
                // save the users details to SharedPreferences so other screens can read them
                val prefs = getSharedPreferences("user", MODE_PRIVATE)

                prefs.edit()
                    .putString("firstName", firstName)
                    .putString("lastName", lastName)
                    .putString("email", email)
                    .putString("password", password)
                    .apply()

                // OPEN MAIN ACTIVITY
                // close this screen after navigating so the user cant press back to the sign up screen
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    // creates a rounded rectangle background shape with a given fill color
    private fun roundedBg(color: String, radius: Float): GradientDrawable {

        return GradientDrawable().apply {

            setColor(Color.parseColor(color))
            cornerRadius = radius * resources.displayMetrics.density // convert dp to pixels
        }
    }
}