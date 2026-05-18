package com.example.coffeelab_app2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

// this is the home screen of the app
class MainActivity : AppCompatActivity() {

    // declared up here so both onCreate and the load functions can access them
    private lateinit var drinksRow: LinearLayout
    private lateinit var popularRow: LinearLayout

    // each category is a list of Drink objects — Drink is our custom data class defined at the bottom
    private val hotCoffee = listOf(
        Drink("Cappuccino", "With steamed milk", R.drawable.cappuccino, "#FFA000", "Hot Coffee"),
        Drink("Latte", "With full cream milk", R.drawable.latte, "#8F2416", "Hot Coffee"),
        Drink("Americano", "With zero sugar", R.drawable.americano, "#FFA000", "Hot Coffee"),
        Drink("Mocha", "Chocolate coffee", R.drawable.mocha, "#8F2416", "Hot Coffee")
    )

    private val coldCoffee = listOf(
        Drink("Iced Latte", "Cold milk coffee", R.drawable.icedlatte, "#FFA000", "Cold Coffee"),
        Drink("Iced Spanish Latte", "Sweet iced coffee", R.drawable.icedspanishlatte, "#8F2416", "Cold Coffee"),
        Drink("Cold Brew", "Smooth and bold", R.drawable.coldbrew, "#FFA000", "Cold Coffee"),
        Drink("Iced Pistachio Latte", "Nutty iced latte", R.drawable.icedpistachiolatte, "#8F2416", "Cold Coffee")
    )

    private val others = listOf(
        Drink("Chocolate Milkshake", "Creamy chocolate drink", R.drawable.chocolatemilkshake, "#FFA000", "Others"),
        Drink("Classic Mojito", "Fresh mint drink", R.drawable.classicmojito, "#8F2416", "Others"),
        Drink("Lemon Mint", "Cool citrus mint", R.drawable.lemonmint, "#FFA000", "Others"),
        Drink("Watermelon Juice", "Fresh fruit drink", R.drawable.watermelonjuice, "#8F2416", "Others")
    )

    // these are the featured drinks shown in the bigger cards section
    // they use extra fields like cardImage and selectedMilk that regular drinks dont need
    private val popularDrinks = listOf(
        Drink(
            "Latte",
            "With full cream milk",
            R.drawable.latte,
            "#FFA000",
            "Hot Coffee",
            cardImage = R.drawable.lattecream,
            selectedMilk = "Whole Milk"
        ),

        Drink(
            "Americano",
            "With zero sugar",
            R.drawable.americano,
            "#FFA000",
            "Hot Coffee",
            cardImage = R.drawable.americanocard,
            sweetnessLevel = 0
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)

        // load the users first name from SharedPreferences — defaults to "User" if nothing was saved
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val firstName = prefs.getString("firstName", "User")

        // animate the welcome text typing itself out letter by letter
        typeWriter(welcomeText, "Welcome, $firstName!")

        welcomeText.setTypeface(null, Typeface.BOLD)

        drinksRow = findViewById(R.id.drinksRow)
        popularRow = findViewById(R.id.popularRow)

        val hotCoffeeTab = findViewById<TextView>(R.id.hotCoffeeTab)
        val coldCoffeeTab = findViewById<TextView>(R.id.coldCoffeeTab)
        val othersTab = findViewById<TextView>(R.id.othersTab)

        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<ImageView>(R.id.instructionsIcon).setOnClickListener {
            startActivity(Intent(this, Instructions::class.java))
        }

        findViewById<LinearLayout>(R.id.discoverNav).setOnClickListener {
            startActivity(Intent(this, allproducts::class.java))
        }

        findViewById<LinearLayout>(R.id.activityNav).setOnClickListener {
            startActivity(Intent(this, ArchiveActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.settingsNav).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // load hot coffee by default when the screen first opens
        loadDrinks(hotCoffee)
        loadPopular()

        // highlight the hot coffee tab as selected on launch
        selectTab(hotCoffeeTab, coldCoffeeTab, othersTab)

        // each tab click swaps the drinks row and updates which tab looks selected
        hotCoffeeTab.setOnClickListener {
            loadDrinks(hotCoffee)
            selectTab(hotCoffeeTab, coldCoffeeTab, othersTab)
        }

        coldCoffeeTab.setOnClickListener {
            loadDrinks(coldCoffee)
            selectTab(coldCoffeeTab, hotCoffeeTab, othersTab)
        }

        othersTab.setOnClickListener {
            loadDrinks(others)
            selectTab(othersTab, hotCoffeeTab, coldCoffeeTab)
        }
    }

    // types out a string one character at a time to create a typewriter effect
    // delay controls how many milliseconds to wait between each character
    private fun typeWriter(
        textView: TextView,
        text: String,
        delay: Long = 70L
    ) {

        textView.text = ""

        var index = 0

        val handler = Handler(Looper.getMainLooper())

        // this Runnable calls itself repeatedly, adding one character each time until the full text is shown
        val runnable = object : Runnable {

            override fun run() {

                if (index <= text.length) {

                    textView.text = text.substring(0, index) // show text up to the current index

                    index++

                    handler.postDelayed(this, delay) // schedule the next character
                }
            }
        }

        handler.post(runnable)
    }

    // opens the product detail screen and passes along the drinks info
    // sweetnessLevel uses -1 as a "not set" value since we cant pass null through an Intent
    private fun openDetail(drink: Drink) {

        val intent = Intent(this, ProductDetailActivity::class.java).apply {

            putExtra("productName", drink.name)
            putExtra("productCategory", drink.category)
            putExtra("productImage", drink.image)
            putExtra("selectedMilk", drink.selectedMilk)
            putExtra("sweetnessLevel", drink.sweetnessLevel ?: -1)
        }

        startActivity(intent)
    }

    // clears the drinks row and rebuilds it with whichever list was passed in
    private fun loadDrinks(drinks: List<Drink>) {

        drinksRow.removeAllViews()

        for (drink in drinks) {

            // each drink is a vertical layout with a colored image box on top and a name label below
            val item = LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER

                layoutParams = LinearLayout.LayoutParams(
                    dp(150),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = dp(24)
                }

                setOnClickListener { openDetail(drink) }
            }

            // the colored rounded box that sits behind the drink image
            val imageBox = FrameLayout(this).apply {

                layoutParams = LinearLayout.LayoutParams(
                    dp(150),
                    dp(150)
                )

                background = roundedBg(drink.color, 18f)
            }

            val image = ImageView(this).apply {

                setImageResource(drink.image)

                scaleType = ImageView.ScaleType.FIT_CENTER

                layoutParams = FrameLayout.LayoutParams(
                    dp(130),
                    dp(110),
                    Gravity.CENTER
                )
            }

            val title = TextView(this).apply {

                text = drink.name
                textSize = 18f

                gravity = Gravity.CENTER

                setTypeface(null, Typeface.BOLD)

                setTextColor(Color.parseColor("#8F2416"))

                setPadding(0, dp(12), 0, 0)
            }

            imageBox.addView(image)

            item.addView(imageBox)
            item.addView(title)

            drinksRow.addView(item)
        }
    }

    // builds the popular/featured drinks section with larger cards
    private fun loadPopular() {

        popularRow.removeAllViews()

        for (drink in popularDrinks) {

            // each popular card is a big FrameLayout so the image can fill the whole card
            val card = FrameLayout(this).apply {

                background = roundedBg("#FFA000", 18f)

                layoutParams = LinearLayout.LayoutParams(
                    dp(300),
                    dp(400)
                ).apply {
                    marginEnd = dp(22)
                }

                setOnClickListener { openDetail(drink) }
            }

            val image = ImageView(this).apply {

                // use the special card image if one exists, otherwise fall back to the regular drink image
                setImageResource(drink.cardImage ?: drink.image)

                scaleType = ImageView.ScaleType.CENTER_CROP // crop to fill the card without stretching

                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            card.addView(image)

            popularRow.addView(card)
        }
    }

    // highlights the selected tab and resets the other two to look unselected
    private fun selectTab(
        selected: TextView,
        first: TextView,
        second: TextView
    ) {

        selected.background = roundedBg("#F3F0EE", 18f)

        selected.setTextColor(Color.parseColor("#8F2416"))

        selected.setTypeface(null, Typeface.BOLD)

        first.background = null
        second.background = null

        first.setTextColor(Color.parseColor("#8F2416"))
        second.setTextColor(Color.parseColor("#8F2416"))

        first.setTypeface(null, Typeface.BOLD)
        second.setTypeface(null, Typeface.BOLD)
    }

    // creates a rounded rectangle background shape with a given color
    private fun roundedBg(
        color: String,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(Color.parseColor(color))

            cornerRadius = radius * resources.displayMetrics.density // convert dp to pixels
        }
    }

    // converts dp to pixels so sizes look consistent across different screen densities
    private fun dp(value: Int): Int {

        return (value * resources.displayMetrics.density).toInt()
    }

    // data class that holds all the info for a single drink
    // fields with "?" are optional / not every drink needs a cardImage or sweetnessLevel
    data class Drink(
        val name: String,
        val desc: String,
        val image: Int,
        val color: String,
        val category: String,
        val cardImage: Int? = null,
        val selectedMilk: String? = null,
        val sweetnessLevel: Int? = null
    )
}