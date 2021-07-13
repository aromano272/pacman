package com.andreromano.pacman.extensions

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
import com.andreromano.pacman.Vec2
import kotlin.math.roundToInt

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToInt() / multiplier
}

val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

fun Rect.scale(factor: Float): Rect {
    val newWidth = width() * factor
    val newHeight = height() * factor

    val deltaWidth = width() - newWidth
    val deltaHeight = height() - newHeight

    return Rect(
        (left + deltaWidth / 2).toInt(),
        (top + deltaHeight / 2).toInt(),
        (right - deltaWidth / 2).toInt(),
        (bottom - deltaHeight / 2).toInt(),
    )
}

fun RectF.scale(factor: Float): RectF {
    val newWidth = width() * factor
    val newHeight = height() * factor

    val deltaWidth = width() - newWidth
    val deltaHeight = height() - newHeight

    return RectF(
        (left + deltaWidth / 2),
        (top + deltaHeight / 2),
        (right - deltaWidth / 2),
        (bottom - deltaHeight / 2),
    )
}

val Rect.topLeft: Vec2
    get() = Vec2(left, top)

val Rect.topRight: Vec2
    get() = Vec2(right, top)

val Rect.bottomLeft: Vec2
    get() = Vec2(left, bottom)

val Rect.bottomRight: Vec2
    get() = Vec2(right, bottom)

fun Rect.intersects(other: Rect): Boolean = this.intersects(other.left, other.top, other.right, other.bottom)

fun RectF.intersects(other: RectF): Boolean = this.intersects(other.left, other.top, other.right, other.bottom)

fun Rect.copy(left: Int = this.left, top: Int = this.top, right: Int = this.right, bottom: Int = this.bottom): Rect = Rect(left, top, right, bottom)

fun RectF.copy(left: Float = this.left, top: Float = this.top, right: Float = this.right, bottom: Float = this.bottom): RectF = RectF(left, top, right, bottom)
