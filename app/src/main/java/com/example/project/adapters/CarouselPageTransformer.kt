package com.example.project.adapters

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CarouselPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val scaleFactor = 0.8f + (1 - abs(position)) * 0.15f
        page.scaleY = scaleFactor
        page.scaleX = scaleFactor

        page.translationX = -position * page.width * 0.1f

        val alphaFactor = 0.5f + (1 - abs(position)) * 0.5f
        page.alpha = alphaFactor
    }
}
