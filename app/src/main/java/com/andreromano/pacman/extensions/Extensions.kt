package com.andreromano.pacman.extensions

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
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