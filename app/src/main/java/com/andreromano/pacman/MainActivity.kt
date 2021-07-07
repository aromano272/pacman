package com.andreromano.pacman

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_up.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> renderView.onViewEvent(Game.ViewEvent.UP_ARROW_PRESSED)
                MotionEvent.ACTION_UP -> renderView.onViewEvent(Game.ViewEvent.UP_ARROW_RELEASED)
            }
            false
        }
        btn_down.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> renderView.onViewEvent(Game.ViewEvent.DOWN_ARROW_PRESSED)
                MotionEvent.ACTION_UP -> renderView.onViewEvent(Game.ViewEvent.DOWN_ARROW_RELEASED)
            }
            false
        }
        btn_left.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> renderView.onViewEvent(Game.ViewEvent.LEFT_ARROW_PRESSED)
                MotionEvent.ACTION_UP -> renderView.onViewEvent(Game.ViewEvent.LEFT_ARROW_RELEASED)
            }
            false
        }
        btn_right.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> renderView.onViewEvent(Game.ViewEvent.RIGHT_ARROW_PRESSED)
                MotionEvent.ACTION_UP -> renderView.onViewEvent(Game.ViewEvent.RIGHT_ARROW_RELEASED)
            }
            false
        }

//        btn_up.setOnClickListener {
//            renderView.directionChanged(Game.Direction.UP)
//        }
//        btn_down.setOnClickListener {
//            renderView.directionChanged(Game.Direction.DOWN)
//        }
//        btn_left.setOnClickListener {
//            renderView.directionChanged(Game.Direction.LEFT)
//        }
//        btn_right.setOnClickListener {
//            renderView.directionChanged(Game.Direction.RIGHT)
//        }
    }


    private val downArrowReleasedRunnable = Runnable {
        renderView.onViewEvent(Game.ViewEvent.DOWN_ARROW_RELEASED)
    }
    private val leftArrowReleasedRunnable = Runnable {
        renderView.onViewEvent(Game.ViewEvent.LEFT_ARROW_RELEASED)
    }
    private val rightArrowReleasedRunnable = Runnable {
        renderView.onViewEvent(Game.ViewEvent.RIGHT_ARROW_RELEASED)
    }
    private val upArrowReleasedRunnable = Runnable {
        renderView.onViewEvent(Game.ViewEvent.UP_ARROW_RELEASED)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.e("onKeyDown $event")
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                handler.removeCallbacks(downArrowReleasedRunnable)
                renderView.onViewEvent(Game.ViewEvent.DOWN_ARROW_PRESSED)
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                handler.removeCallbacks(leftArrowReleasedRunnable)
                renderView.onViewEvent(Game.ViewEvent.LEFT_ARROW_PRESSED)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                handler.removeCallbacks(rightArrowReleasedRunnable)
                renderView.onViewEvent(Game.ViewEvent.RIGHT_ARROW_PRESSED)
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                handler.removeCallbacks(upArrowReleasedRunnable)
                renderView.onViewEvent(Game.ViewEvent.UP_ARROW_PRESSED)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.e("onKeyUp $event")
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> handler.postDelayed(downArrowReleasedRunnable, 50)
            KeyEvent.KEYCODE_DPAD_LEFT -> handler.postDelayed(leftArrowReleasedRunnable, 50)
            KeyEvent.KEYCODE_DPAD_RIGHT -> handler.postDelayed(rightArrowReleasedRunnable, 50)
            KeyEvent.KEYCODE_DPAD_UP -> handler.postDelayed(upArrowReleasedRunnable, 50)
        }
        return super.onKeyUp(keyCode, event)
    }
}