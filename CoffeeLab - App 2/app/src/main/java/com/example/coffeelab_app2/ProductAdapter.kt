package com.example.coffeelab_app2

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// this adapter feeds the product grid in the all products screen
// it takes a list of products and a click handler (onClick) that runs when a product is tapped
class ProductAdapter(
    private var products: List<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // ViewHolder holds references to the views inside one product card
    // "inner class" means it can access the outer adapter's properties if needed
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productCard: LinearLayout = itemView.findViewById(R.id.productCard)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val reviewButton: TextView = itemView.findViewById(R.id.reviewButton)
    }

    // called when the recyclerview needs a new card view — we inflate the xml layout and wrap it in a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)

        return ProductViewHolder(view)
    }

    // called when a card is about to appear on screen — we fill it with the right product's data
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position] // get the product that matches this position in the list

        holder.productName.text = product.name
        holder.productName.setTypeface(null, Typeface.BOLD)
        holder.productImage.setImageResource(product.image)
        holder.reviewButton.setTypeface(null, Typeface.BOLD)

        // give the card a transparent background with a rounded border
        holder.productCard.background = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke(4, Color.parseColor("#B98B86"))
            cornerRadius = 18f * holder.itemView.resources.displayMetrics.density // convert dp to pixels
        }

        // both the card and the review button do the same thing — open the product detail screen
        holder.productCard.setOnClickListener {
            onClick(product)
        }

        holder.reviewButton.setOnClickListener {
            onClick(product)
        }
    }

    // tells the recyclerview how many items to show
    override fun getItemCount(): Int = products.size

    // replaces the current list with a new one and refreshes the grid
    // called when the user switches tabs or types in the search bar
    fun updateList(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged() // tells the recyclerview to redraw everything with the new list
    }
}