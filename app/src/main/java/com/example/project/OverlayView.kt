package com.example.project

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var faces: List<Face> = emptyList()
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    fun setFaces(faces: List<Face>) {
        this.faces = faces
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (face in faces) {
            drawFace(canvas, face)
        }
    }

    private fun drawFace(canvas: Canvas, face: Face) {
        val bounds = face.boundingBox
        canvas.drawRect(bounds, paint)

        for (landmark in face.allLandmarks) {
            drawLandmark(canvas, landmark)
        }
    }

    private fun drawLandmark(canvas: Canvas, landmark: FaceLandmark) {
        val position = landmark.position
        canvas.drawCircle(position.x, position.y, 10f, paint)
    }
}
