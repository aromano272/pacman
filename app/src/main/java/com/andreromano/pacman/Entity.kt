package com.andreromano.pacman

import android.graphics.Canvas
import android.graphics.Rect

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
        get() = Rect(screenPos.x, screenPos.y, screenPos.x + width, screenPos.y + height)

    val tileRect: Rect
        get() = Rect(tilePos.x * width, tilePos.y * height, tilePos.x * width + width, tilePos.y * height + height)

    abstract fun updateAndRender(canvas: Canvas)

}