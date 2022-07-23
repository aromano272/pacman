package com.andreromano.pacman

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.toRectF

abstract class Entity(
    var x: Int,
    var y: Int,
    var tileX: Int,
    var tileY: Int,
    var width: Int,
    var height: Int
) {

    private val _rect = Rect()
    val hitbox: Rect
        get() = _rect.apply {
            left = x - width / 2
            top = y - height / 2
            right = x + width / 2
            bottom = y + height / 2
        }

    fun createTestHitbox(x: Int, y: Int): Rect = Rect(
        x - width / 2,
        y - height / 2,
        x + width / 2,
        y + height / 2
    )

    fun currentPos(): Position = Position(x, y, tileX, tileY)

    fun updatePos(pos: Position) {
        x = pos.x
        y = pos.y
        tileX = pos.tileX
        tileY = pos.tileY
    }

    abstract fun updateAndRender(canvas: Canvas)

}

data class Position(
    val x: Int,
    val y: Int,
    val tileX: Int,
    val tileY: Int
)