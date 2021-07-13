package com.andreromano.pacman

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.toRectF

abstract class Entity(
    var screenPos: Vec2,
    var tilePos: Vec2,
    val width: Int,
    val height: Int,
) {

    constructor(screenX: Int, screenY: Int, tileX: Int, tileY: Int, width: Int, height: Int) :
            this(Vec2(screenX, screenY), Vec2(tileX, tileY), width, height)

    var x: Int
        get() = screenPos.x
        set(value) {
            screenPos.x = value
        }

    var y: Int
        get() = screenPos.y
        set(value) {
            screenPos.y = value
        }

    var tileX: Int
        get() = tilePos.x
        set(value) {
            tilePos.x = value
        }

    var tileY: Int
        get() = tilePos.y
        set(value) {
            tilePos.y = value
        }

    val screenRect: Rect
        get() = Rect(x - width / 2, y - width / 2, x + width / 2, y + width / 2)

    open val canvasRect: RectF
        get() = screenRect.toRectF()

    val tileRect: Rect
        get() = Rect(tileX * width, tileY * height, tileX * width + width, tileY * height + height)

    abstract fun updateAndRender(canvas: Canvas)

}