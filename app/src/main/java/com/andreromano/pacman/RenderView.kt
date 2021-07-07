package com.andreromano.pacman

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.andreromano.pacman.extensions.round
import com.andreromano.pacman.extensions.toPx
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt


class RenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SurfaceView(context, attrs, defStyleAttr), Runnable {

    private var isRunning: Boolean = false
    private lateinit var thread: Thread
    private val game: Game = Game()

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        color = Color.WHITE
    }

    private val redPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val greenPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    override fun run() {
        while (isRunning) {
            if (!holder.surface.isValid) return
            val canvas = holder.lockHardwareCanvas()

            val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())

            canvas.drawRect(rect, paint)
            game.updateAndRender(canvas)

            drawFps(canvas)


            holder.unlockCanvasAndPost(canvas)
            frameCount++
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        game.sceneSizeChanged(w, h)
    }

    private var frameCount: Long = 0
    private var last100FrameTimes: LinkedList<Long> = LinkedList()
    private var lastFrameMs: Long = System.currentTimeMillis()
    private var medianFrameTime: Long = 0
    private var minFrameTime: Long = 0
    private var maxFrameTime: Long = 0

    private fun drawFps(canvas: Canvas) {
        val currFrameMs = System.currentTimeMillis()
        val lastFrameMs = lastFrameMs
        this.lastFrameMs = currFrameMs
        val currFrameTime = (currFrameMs - lastFrameMs)

        if (last100FrameTimes.size == 100) last100FrameTimes.removeFirst()
        last100FrameTimes.addLast(currFrameTime)

        if (frameCount % 60 == 0L) {
            val sortedFrameTimes = last100FrameTimes.sorted()
            medianFrameTime = sortedFrameTimes.getOrNull(50) ?: 0
            minFrameTime = sortedFrameTimes.firstOrNull() ?: 0
            maxFrameTime = sortedFrameTimes.lastOrNull() ?: 0
        }

        val fps = (1000.0 / (medianFrameTime)).round(1)

        canvas.drawText("FPS: $fps", 50f, 50f, textPaint)
        canvas.drawText("Min FT: $minFrameTime", 50f, 50f + textPaint.textSize, textPaint)
        canvas.drawText("Max FT: $maxFrameTime", 50f, 50f + textPaint.textSize * 2, textPaint)
    }



    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                resume()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                pause()
            }
        })
    }

    /**
     * Called by MainActivity.onPause() to stop the thread.
     */
    private fun pause() {
        isRunning = false
        try {
            // Stop the thread == rejoin the main thread.
            thread.join()
        } catch (e: InterruptedException) {
        }
    }

    /**
     * Called by MainActivity.onResume() to start a thread.
     */
    private fun resume() {
        isRunning = true
        thread = Thread(this)
        thread.start()
    }

    fun directionChanged(direction: Game.Direction) {
        game.directionChanged(direction)
    }

    fun onViewEvent(viewEvent: Game.ViewEvent) {
        game.onViewEvent(viewEvent)
    }
}