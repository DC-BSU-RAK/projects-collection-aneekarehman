package com.example.coffeelab_app2

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

// this is the screen that shows the full details of a drink and lets the user customise it before reviewing
class ProductDetailActivity : AppCompatActivity() {

    // tracks whether the user has favourited this drink — starts as false
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // read the data that was passed in from the previous screen via Intent
        // the "?: " part provides a fallback value in case nothing was passed
        val productName =
            intent.getStringExtra("productName") ?: "Coffee"

        val productCategory =
            intent.getStringExtra("productCategory") ?: "All Drinks"

        val productImage =
            intent.getIntExtra("productImage", R.drawable.cappuccino)

        val selectedMilk =
            intent.getStringExtra("selectedMilk")

        // -1 is used as a "not set" value since Intents cant pass null for integers
        val sweetnessLevel =
            intent.getIntExtra("sweetnessLevel", -1)

        val nameText =
            findViewById<TextView>(R.id.productName)

        val descriptionText =
            findViewById<TextView>(R.id.productDescription)

        val categoryLabel =
            findViewById<TextView>(R.id.categoryLabel)

        val imageView =
            findViewById<ImageView>(R.id.productImage)

        val favoriteButton =
            findViewById<TextView>(R.id.favoriteButton)

        val backButton =
            findViewById<ImageView>(R.id.backButton)

        val enterReviewButton =
            findViewById<Button>(R.id.enterReviewButton)

        val sweetnessSlider =
            findViewById<SeekBar>(R.id.sweetnessSlider)

        val sweetnessText =
            findViewById<TextView>(R.id.sweetnessText)

        // fill in the basic product info at the top of the screen
        nameText.text = productName
        nameText.setTypeface(null, Typeface.BOLD)

        // getProductDescription() returns a hardcoded description based on the drink name
        descriptionText.text = getProductDescription(productName)
        descriptionText.setTypeface(null, Typeface.BOLD)

        categoryLabel.text = productCategory
        categoryLabel.setTypeface(null, Typeface.BOLD)

        imageView.setImageResource(productImage)

        categoryLabel.background = roundedBg("#FFE8A3", 24f)

        // ColorStateList is needed here because Button backgrounds work differently to regular views
        enterReviewButton.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#FFE8A3"))

        enterReviewButton.setTextColor(Color.parseColor("#8F2416"))
        enterReviewButton.setTypeface(null, Typeface.BOLD)

        val priceInput =
            findViewById<EditText>(R.id.priceInput)

        val storeInput =
            findViewById<EditText>(R.id.storeInput)

        priceInput.background = roundedBg("#FFF8F1", 8f)
        storeInput.background = roundedBg("#FFF8F1", 8f)

        priceInput.setTypeface(null, Typeface.BOLD)
        storeInput.setTypeface(null, Typeface.BOLD)

        // collect all the option cards into lists so we can style and handle them together
        val sizeOptions = listOf(
            findViewById<LinearLayout>(R.id.smallSize),
            findViewById<LinearLayout>(R.id.mediumSize),
            findViewById<LinearLayout>(R.id.largeSize),
            findViewById<LinearLayout>(R.id.xlSize)
        )

        val milkOptions = listOf(
            findViewById<TextView>(R.id.wholeMilk),
            findViewById<TextView>(R.id.oatMilk),
            findViewById<TextView>(R.id.almondMilk),
            findViewById<TextView>(R.id.soyMilk)
        )

        val brewOptions = listOf(
            findViewById<TextView>(R.id.espressoMachine),
            findViewById<TextView>(R.id.frenchPress),
            findViewById<TextView>(R.id.dripMachine),
            findViewById<TextView>(R.id.pourOver)
        )

        // apply the default unselected style to all option cards
        styleLayoutCards(sizeOptions)
        styleTextCards(milkOptions)
        styleTextCards(brewOptions)

        // make each card selectable — tapping one highlights it and resets the others
        setSelectableLayoutCards(sizeOptions)
        setSelectableTextCards(milkOptions)
        setSelectableTextCards(brewOptions)

        // PRESELECT WHOLE MILK

        // if the drink was opened with whole milk already chosen (e.g. from the popular section), pre-highlight it
        if (selectedMilk == "Whole Milk") {

            val wholeMilk =
                findViewById<TextView>(R.id.wholeMilk)

            milkOptions.forEach {
                it.background = cardBg(false)
                it.setTextColor(Color.parseColor("#E8D7CE"))
            }

            wholeMilk.background = cardBg(true)
            wholeMilk.setTextColor(Color.parseColor("#2A1713"))
        }

        // SWEETNESS

        // update the sweetness label in real time as the user drags the slider
        sweetnessSlider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    // map the slider value (0-10) to a readable label
                    sweetnessText.text = when {
                        progress <= 0 -> "Zero Sweet"
                        progress <= 3 -> "Low Sweet"
                        progress <= 7 -> "Medium Sweet"
                        else -> "Extra Sweet"
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        // if a sweetness level was passed in, set the slider and label to match it straight away
        if (sweetnessLevel != -1) {

            sweetnessSlider.progress = sweetnessLevel

            sweetnessText.text = when {
                sweetnessLevel <= 0 -> "Zero Sweet"
                sweetnessLevel <= 3 -> "Low Sweet"
                sweetnessLevel <= 7 -> "Medium Sweet"
                else -> "Extra Sweet"
            }
        }

        sweetnessText.setTypeface(null, Typeface.BOLD)

        // PAIRINGS

        val pairings = listOf(
            findViewById<LinearLayout>(R.id.pairCroissant),
            findViewById<LinearLayout>(R.id.pairChocolateCroissant),
            findViewById<LinearLayout>(R.id.pairPancakes),
            findViewById<LinearLayout>(R.id.pairBagels),
            findViewById<LinearLayout>(R.id.pairSalad)
        )

        pairings.forEach {

            it.background = roundedBg("#FFA000", 16f)

            setTextColorInside(it, "#8F2416")

            // tapping a pairing resets all of them then highlights just the tapped one
            it.setOnClickListener { view ->

                pairings.forEach { pair ->
                    pair.background = roundedBg("#FFA000", 16f)
                    setTextColorInside(pair, "#8F2416")
                }

                view.background =
                    roundedStrokeBg("#FFE8A3", "#FFA000", 16f, 3)

                if (view is LinearLayout) {
                    setTextColorInside(view, "#8F2416")
                }
            }

            // make any text inside each pairing card bold
            for (i in 0 until it.childCount) {

                val child = it.getChildAt(i)

                if (child is TextView) {
                    child.setTypeface(null, Typeface.BOLD)
                }
            }
        }

        // FAVORITE BUTTON

        // toggle the heart icon between filled and empty each time its tapped
        favoriteButton.setOnClickListener {

            isFavorite = !isFavorite

            if (isFavorite) {

                favoriteButton.text = "❤"
                favoriteButton.setTextColor(Color.parseColor("#FF4B4B"))
                favoriteButton.textSize = 28f

            } else {

                favoriteButton.text = "♡"
                favoriteButton.setTextColor(Color.parseColor("#FFE8A3"))
                favoriteButton.textSize = 38f
            }
        }

        // BACK

        backButton.setOnClickListener {
            finish()
        }

        // REVIEW PAGE

        // pass all the current customisation choices through to the review screen
        enterReviewButton.setOnClickListener {

            val intent =
                Intent(this, ReviewActivity::class.java)

            intent.putExtra("productName", productName)
            intent.putExtra("productCategory", productCategory)
            intent.putExtra("productImage", productImage)
            intent.putExtra("price", priceInput.text.toString())
            intent.putExtra("store", storeInput.text.toString())
            intent.putExtra("sweetness", sweetnessText.text.toString())

            startActivity(intent)
        }
    }

    // returns a hardcoded description string for each drink name
    // the "else" at the bottom catches any drink that isnt in the list
    private fun getProductDescription(name: String): String {

        return when (name) {

            "Cappuccino" ->
                "Rich espresso topped with warm foamy milk."

            "Latte" ->
                "Smooth espresso blended with creamy milk."

            "Cortando" ->
                "Bold espresso softened with a touch of milk."

            "Espresso" ->
                "A strong, concentrated shot of coffee."

            "Flat White" ->
                "Velvety milk poured over bold espresso."

            "Turkish Coffee" ->
                "Deep, aromatic coffee with a traditional finish."

            "Americano" ->
                "Espresso with hot water for a clean bold taste."

            "Macchiato" ->
                "Espresso marked with a small layer of foam."

            "Mocha" ->
                "Chocolate and espresso mixed into a cozy drink."

            "Piccolo" ->
                "A small latte with a strong espresso flavor."

            "Iced Spanish Latte" ->
                "Sweet chilled coffee with condensed milk."

            "Iced Latte" ->
                "Cold espresso mixed with smooth milk."

            "Iced Pistachio Latte" ->
                "Creamy iced latte with nutty pistachio flavor."

            "Salted Caramel Latte" ->
                "Sweet caramel coffee with a salty finish."

            "Affogato" ->
                "Creamy dessert topped with hot espresso."

            "Cold Brew" ->
                "Slow-brewed coffee with a smooth bold taste."

            "Rose Latte" ->
                "Floral latte with a soft rose sweetness."

            "Chocolate Milkshake" ->
                "Thick chocolate drink blended until creamy."

            "Classic Mojito" ->
                "Refreshing mint drink with citrus notes."

            "Fresh Orange Juice" ->
                "Freshly squeezed orange juice."

            "Lemon Mint" ->
                "Cool lemon drink with fresh mint."

            "Pineapple Juice" ->
                "Tropical pineapple drink with bright sweetness."

            "Watermelon Juice" ->
                "Light, refreshing watermelon drink."

            "Hot Tea" ->
                "Warm brewed tea with a calming aroma."

            "Hot Chocolate" ->
                "Rich cocoa drink with a creamy finish."

            else ->
                "A delicious drink ready to be reviewed."
        }
    }

    // applies the default unselected style to a list of LinearLayout cards
    private fun styleLayoutCards(cards: List<LinearLayout>) {

        cards.forEach {

            it.background = cardBg(false)

            setTextColorInside(it, "#F5E6D3")

            boldTextInside(it)
        }
    }

    // makes each layout card selectable — tapping one highlights it and resets the rest
    private fun setSelectableLayoutCards(cards: List<LinearLayout>) {

        cards.forEach { card ->

            card.setOnClickListener {

                cards.forEach {
                    it.background = cardBg(false)
                    setTextColorInside(it, "#F5E6D3")
                }

                card.background = cardBg(true)

                setTextColorInside(card, "#2A1713")
            }
        }
    }

    // applies the default unselected style to a list of TextView cards
    private fun styleTextCards(cards: List<TextView>) {

        cards.forEach {

            it.background = cardBg(false)

            it.setTextColor(Color.parseColor("#E8D7CE"))

            it.setTypeface(null, Typeface.BOLD)
        }
    }

    // makes each text card selectable — same idea as setSelectableLayoutCards but for TextViews
    private fun setSelectableTextCards(cards: List<TextView>) {

        cards.forEach { card ->

            card.setOnClickListener {

                cards.forEach {
                    it.background = cardBg(false)
                    it.setTextColor(Color.parseColor("#E8D7CE"))
                }

                card.background = cardBg(true)

                card.setTextColor(Color.parseColor("#2A1713"))
            }
        }
    }

    // loops through a LinearLayout's children and sets the text color on any TextViews it finds
    private fun setTextColorInside(
        layout: LinearLayout,
        color: String
    ) {

        for (i in 0 until layout.childCount) {

            val child = layout.getChildAt(i)

            if (child is TextView) {

                child.setTextColor(Color.parseColor(color))
            }
        }
    }

    // same as above but makes the text bold instead of changing color
    private fun boldTextInside(layout: LinearLayout) {

        for (i in 0 until layout.childCount) {

            val child = layout.getChildAt(i)

            if (child is TextView) {

                child.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    // returns a card background shape — orange and filled if selected, transparent with a border if not
    private fun cardBg(selected: Boolean): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                Color.parseColor(
                    if (selected) "#FFA000"
                    else "#00000000"
                )
            )

            cornerRadius = 24f * resources.displayMetrics.density

            setStroke(
                if (selected) 4 else 3,
                Color.parseColor(
                    if (selected) "#FFA000"
                    else "#E8D7CE"
                )
            )
        }
    }

    // creates a plain rounded background with just a fill color
    private fun roundedBg(
        color: String,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(Color.parseColor(color))

            cornerRadius =
                radius * resources.displayMetrics.density
        }
    }

    // same as roundedBg but adds a visible border around the shape
    private fun roundedStrokeBg(
        fillColor: String,
        strokeColor: String,
        radius: Float,
        strokeWidth: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(Color.parseColor(fillColor))

            cornerRadius =
                radius * resources.displayMetrics.density

            setStroke(
                (strokeWidth * resources.displayMetrics.density).toInt(),
                Color.parseColor(strokeColor)
            )
        }
    }
}
