package com.andreromano.pacman

data class Vec2F(
    var x: Float,
    var y: Float,
) {
    fun toInt(): Vec2 = Vec2(x.toInt(), y.toInt())
}