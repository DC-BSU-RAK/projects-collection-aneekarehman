package com.example.coffeelab_app2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

// this is the adapter for the intro slideshow — it feeds images into the ViewPager2
// adapters sit between the data (our image list) and the UI (the viewpager slides)
class IntroAdapter(private val images: List<Int>) :
    RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {

    // ViewHolder holds a reference to the views inside one slide
    // instead of finding views over and over, we find them once here and reuse them
    class IntroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val introImage: ImageView = itemView.findViewById(R.id.introImage)
    }

    // called when a new slide view needs to be created
    // LayoutInflater turns our xml slide layout into an actual view object
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemslide, parent, false)
        return IntroViewHolder(view)
    }

    // called when a slide is about to appear on screen
    // we use the position to grab the right image from our list and put it in the holder
    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        holder.introImage.setImageResource(images[position])
    }

    // tells the adapter how many slides there are / one per image
    override fun getItemCount(): Int = images.size
}