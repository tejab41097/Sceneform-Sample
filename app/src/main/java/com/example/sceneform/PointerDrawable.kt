package com.example.sceneform

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable

class PointerDrawable: Drawable() {
    private val paint: Paint = Paint()
    private var enabled = false

    fun isEnabled(): Boolean {
        return enabled
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
    override fun draw(canvas: Canvas) {
        val cx: Float = (bounds.width() / 2).toFloat()
        val cy: Float = (bounds.height() / 2).toFloat()
        if (enabled) {
            paint.color = Color.GREEN
            canvas.drawCircle(cx, cy, 10F, paint)
        } else {
            paint.color = Color.GRAY
            canvas.drawText("X", cx, cy, paint)
        }    }

    override fun setAlpha(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getOpacity(): Int {
        TODO("Not yet implemented")
    }

    override fun setColorFilter(p0: ColorFilter?) {
        TODO("Not yet implemented")
    }
}