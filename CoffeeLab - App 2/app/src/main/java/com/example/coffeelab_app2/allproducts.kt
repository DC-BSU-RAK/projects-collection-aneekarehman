package com.example.coffeelab_app2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


    // this is the all products page, it showcase the complete list of drinks
class allproducts : AppCompatActivity() {

    // declare the adapter that will manage what shows up in the product grid
    private lateinit var adapter: ProductAdapter

    // list of all hot coffee products / each has a name, category, and image resource.
    private val hotCoffee = listOf(
        Product("Cappuccino", "Hot Coffee", R.drawable.cappuccino),
        Product("Latte", "Hot Coffee", R.drawable.latte),
        Product("Cortando", "Hot Coffee", R.drawable.cortando),
        Product("Espresso", "Hot Coffee", R.drawable.espresso),
        Product("Flat White", "Hot Coffee", R.drawable.flatwhite),
        Product("Turkish Coffee", "Hot Coffee", R.drawable.turkishcoffee),
        Product("Americano", "Hot Coffee", R.drawable.americano),
        Product("Macchiato", "Hot Coffee", R.drawable.macchiato),
        Product("Mocha", "Hot Coffee", R.drawable.mocha),
        Product("Piccolo", "Hot Coffee", R.drawable.piccolo)
    )

    // list of all cold coffee products
    private val coldCoffee = listOf(
        Product("Iced Spanish Latte", "Cold Coffee", R.drawable.icedspanishlatte),
        Product("Iced Latte", "Cold Coffee", R.drawable.icedlatte),
        Product("Iced Pistachio Latte", "Cold Coffee", R.drawable.icedpistachiolatte),
        Product("Salted Caramel Latte", "Cold Coffee", R.drawable.saltedcaramellatte),
        Product("Affogato", "Cold Coffee", R.drawable.affogato),
        Product("Cold Brew", "Cold Coffee", R.drawable.coldbrew),
        Product("Rose Latte", "Cold Coffee", R.drawable.roselatte)
    )

    // list of other non-coffee drinks like juices, teas, and milkshakes
    private val others = listOf(
        Product("Chocolate Milkshake", "Others", R.drawable.chocolatemilkshake),
        Product("Classic Mojito", "Others", R.drawable.classicmojito),
        Product("Fresh Orange Juice", "Others", R.drawable.freshorangejuice),
        Product("Lemon Mint", "Others", R.drawable.lemonmint),
        Product("Pineapple Juice", "Others", R.drawable.pinapplejuice),
        Product("Watermelon Juice", "Others", R.drawable.watermelonjuice),
        Product("Hot Tea", "Others", R.drawable.hottea),
        Product("Hot Chocolate", "Others", R.drawable.hotchocolate)
    )

    // combines all three category lists into one / used for the "All Drinks" tab and search
    private val allProducts: List<Product>
        get() = hotCoffee + coldCoffee + others

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allproducts)

        // used to connect the RecyclerView and SearchView from the XML layout
        val recyclerView = findViewById<RecyclerView>(R.id.productsRecyclerView)
        val searchView = findViewById<SearchView>(R.id.searchView)

        // this grabs all four category tab TextViews so we can switch between them
        val allDrinksTab = findViewById<TextView>(R.id.allDrinksTab)
        val hotCoffeeTab = findViewById<TextView>(R.id.hotCoffeeTab)
        val coldCoffeeTab = findViewById<TextView>(R.id.coldCoffeeTab)
        val othersTab = findViewById<TextView>(R.id.othersTab)

        // grabs the bottom navigation bar items
        val homeNav = findViewById<LinearLayout>(R.id.homeNav)
        val discoverNav = findViewById<LinearLayout>(R.id.discoverNav)
        val activityNav = findViewById<LinearLayout>(R.id.activityNav)
        val settingsNav = findViewById<LinearLayout>(R.id.settingsNav)

        // grasb the profile and instructions icon buttons at the top
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val instructionsIcon = findViewById<ImageView>(R.id.instructionsIcon)

        // this is used to set up the adapter with all products and define what happens when a product is clicked

        // it opens ProductDetailActivity and passes the product's name, category, and image
        adapter = ProductAdapter(allProducts) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)

            intent.putExtra("productName", product.name)
            intent.putExtra("productCategory", product.category)
            intent.putExtra("productImage", product.image)

            startActivity(intent)
        }

        // uses a 2-column grid layout for displaying the product cards
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        // highlighting "All Drinks" as the default selected tab when the screen first opens
        selectTab(allDrinksTab, hotCoffeeTab, coldCoffeeTab, othersTab)

        // when "All Drinks" tab is clicked, show everything and highlight that tab
        allDrinksTab.setOnClickListener {
            adapter.updateList(allProducts)
            selectTab(allDrinksTab, hotCoffeeTab, coldCoffeeTab, othersTab)
        }

        // filter to hot coffee only when that tab is clicked
        hotCoffeeTab.setOnClickListener {
            adapter.updateList(hotCoffee)
            selectTab(hotCoffeeTab, allDrinksTab, coldCoffeeTab, othersTab)
        }

        // filter to cold coffee only when that tab is clicked
        coldCoffeeTab.setOnClickListener {
            adapter.updateList(coldCoffee)
            selectTab(coldCoffeeTab, allDrinksTab, hotCoffeeTab, othersTab)
        }

        // filter to "Others" category when that tab is clicked
        othersTab.setOnClickListener {
            adapter.updateList(others)
            selectTab(othersTab, allDrinksTab, hotCoffeeTab, coldCoffeeTab)
        }

        // listens for changes in the search bar as the user types
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                val filtered = allProducts.filter {

                    it.name.contains(newText ?: "", ignoreCase = true) ||
                            it.category.contains(newText ?: "", ignoreCase = true)
                }

                // Push the filtered results to the adapter so the grid updates
                adapter.updateList(filtered)

                return true
            }
        })

        /* PROFILE BUTTON */

        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        /* INSTRUCTIONS BUTTON */

        // Navigate to the instructions screen when that icon is tapped
        instructionsIcon.setOnClickListener {
            startActivity(Intent(this, Instructions::class.java))
        }

        /* BOTTOM NAVIGATION */

        // Go back to the home / main screen and remove this activity from the back stack
        homeNav.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // "Discover" just resets the view back to showing all products
        discoverNav.setOnClickListener {
            adapter.updateList(allProducts)
            selectTab(allDrinksTab, hotCoffeeTab, coldCoffeeTab, othersTab)
        }

        // Navigate to the order archive / history screen
        activityNav.setOnClickListener {
            startActivity(Intent(this, ArchiveActivity::class.java))
        }

        // Navigate to the settings screen
        settingsNav.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // Visually highlights the selected tab with a background and de-styles the others
    private fun selectTab(
        selected: TextView,
        tab1: TextView,
        tab2: TextView,
        tab3: TextView
    ) {

        // Give the selected tab a rounded background to show it's active
        selected.background = roundedBg("#EFEAE7", 20f)
        selected.setTextColor(Color.parseColor("#8F2416"))
        selected.setTypeface(null, Typeface.BOLD)

        // Remove the background from unselected tabs so they look inactive
        listOf(tab1, tab2, tab3).forEach {

            it.background = null
            it.setTextColor(Color.parseColor("#8F2416"))
            it.setTypeface(null, Typeface.BOLD)
        }
    }

    // Helper function to create a rounded rectangle background with a given color and corner radius
    private fun roundedBg(color: String, radius: Float): GradientDrawable {

        return GradientDrawable().apply {

            setColor(Color.parseColor(color))
            // Multiply by display density so the radius scales correctly across screen sizes
            cornerRadius = radius * resources.displayMetrics.density
        }
    }
}