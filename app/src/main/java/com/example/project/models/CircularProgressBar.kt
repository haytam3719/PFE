package com.example.project.models

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.project.R
class CircularProgressView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    var progress: Int = 0
    private val maxProgress = 100
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        isAntiAlias = true
    }

    fun updateProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY) - paint.strokeWidth

        // Draw background circle
        paint.color = ContextCompat.getColor(context, R.color.lighter_grey)
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw progress arc
        paint.color = ContextCompat.getColor(context, R.color.orange)
        val sweepAngle = (360 * progress / maxProgress).toFloat()
        canvas.drawArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            -90f, sweepAngle, false, paint
        )
    }
}
