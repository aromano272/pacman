package com.andreromano.pacman

data class Vec2(
    var x: Int,
    var y: Int,
) {
    fun toFloat(): Vec2F = Vec2F(x.toFloat(), y.toFloat())
}

