package com.andreromano.pacman.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toRectF
import com.andreromano.pacman.Entity

class WallEntity(
    x: Int,
    y: Int,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int
) : Entity(x, y, tileX, tileY, width, height) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    override fun update(canvas: Canvas) {
    }

    override fun render(canvas: Canvas) {
        canvas.drawRect(hitbox.toRectF(), paint)
    }
}