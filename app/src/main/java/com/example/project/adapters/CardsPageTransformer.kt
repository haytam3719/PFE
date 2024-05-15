package com.example.project.adapters

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CardsPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val scaleFactor = 0.85f + (1 - abs(position)) * 0.15f
        page.scaleY = scaleFactor
        page.scaleX = scaleFactor

        page.translationX = -position * page.width * 0.1f

        val alphaFactor = 0.3f + (1 - abs(position)) * 0.7f
        page.alpha = alphaFactor
    }
}
