package com.example.project.adapters

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class CarouselPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val minScale = 0.85f
        val maxScale = 1.0f
        val scaleFactor = if (position == 0.0f) maxScale else minScale

        page.apply {
            scaleX = scaleFactor
            scaleY = scaleFactor
            alpha = 0.5f + (1 - kotlin.math.abs(position)) * 0.5f
        }
    }
}
