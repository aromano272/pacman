package com.andreromano.pacman

import android.graphics.Canvas

abstract class Entity(
    var x: Float,
    var y: Float,
) {

    abstract fun updateAndRender(canvas: Canvas)

}