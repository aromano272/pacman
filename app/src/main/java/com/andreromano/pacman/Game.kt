package com.andreromano.pacman

import android.graphics.*
import com.andreromano.pacman.extensions.scale
import com.andreromano.pacman.extensions.toPx
import kotlin.math.*

class Game {

    enum class Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }



    private val gameboard = """
        ############################
        #............##............#
        #.####.#####.##.#####.####.#
        #o####.#####.##.#####.####o#
        #.####.#####.##.#####.####.#
        #..........................#
        #.####.##.########.##.####.#
        #.####.##.########.##.####.#
        #......##....##....##......#
        ######.#####.##.#####.######
             #.#####.##.#####.#     
             #.##..........##.#     
             #.##.###  ###.##.#     
        ######.##.#      #.##.######
                  #      #          
        ######.##.#      #.##.######
             #.##.########.##.#     
             #.##..........##.#     
             #.##.########.##.#     
        ######.##.########.##.######
        #............##............#
        #.####.#####.##.#####.####.#
        #.####.#####.##.#####.####.#
        #o..##.......P........##..o#
        ###.##.##.########.##.##.###
        ###.##.##.########.##.##.###
        #......##....##....##......#
        ############################
    """.trimIndent()

    private var sceneWidth = 0f
    private var sceneHeight = 0f

    private var frameCount: Long = 0

    private var direction: Direction = Direction.RIGHT

    private var score: Int = 0

    private var pacman: PacmanEntity? = null
    private val entities = mutableListOf<Entity>()
    private lateinit var entitiesMap: Array<Array<Entity?>>

    private val redStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 2f
    }

    private val greenPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val bluePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    private val yellowPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
    }

    inner class PacmanEntity(x: Float, y: Float) : Entity(x, y) {
        private val width = entityWidth
        private val height = entityHeight

        val velocity = 4

        override fun updateAndRender(canvas: Canvas) {
            if (pressedButtonDirection != null) {
                var newX = x
                var newY = y
                when (pressedButtonDirection) {
                    Direction.UP -> newY = y - velocity
                    Direction.RIGHT -> newX = x + velocity
                    Direction.DOWN -> newY = y + velocity
                    Direction.LEFT -> newX = x - velocity
                }

                val testEntity = getEntityFromPos(newX, newY)
                if (testEntity !is WallEntity) {
                    x = newX
                    y = newY

                    if (x >= sceneWidth) {
                        x -= sceneWidth
                    }
                    if (x < 0) {
                        x += sceneWidth
                    }
                    if (y >= sceneHeight) {
                        y -= sceneHeight
                    }
                    if (y < 0) {
                        y += sceneHeight
                    }
                }
                if (testEntity is FoodEntity) {
                    score += 100
                    removeEntity(testEntity)
                }
            }

            val right = width + x
            val bottom = height + y

            val rect = RectF(x, y, right.coerceAtMost(sceneWidth), bottom.coerceAtMost(sceneHeight)).scale(0.75f)
            canvas.drawOval(rect, yellowPaint)
            // enables wrapping around the scene
            if (right > sceneWidth) canvas.drawOval(RectF(0f, y, right - sceneWidth, bottom).scale(0.75f), yellowPaint)
            if (bottom > sceneHeight) canvas.drawOval(RectF(x, 0f, right, bottom - sceneHeight).scale(0.75f), yellowPaint)
            if (right > sceneWidth && bottom > sceneHeight) canvas.drawOval(RectF(0f, 0f, right - sceneWidth, bottom - sceneHeight).scale(0.75f), yellowPaint)

            // test entity grid bounding box
            val testMemX = getMemXFromPos(x)
            val testMemY = getMemYFromPos(y)
            val testX = testMemX * entityWidth
            val testY = testMemY * entityHeight
            canvas.drawRect(RectF(testX, testY, testX + entityWidth, testY + entityHeight), redStrokePaint)
        }
    }


    private var entityWidth = 0f
    private var entityHeight = 0f
    private var mapWidth = 0
    private var mapHeight = 0

    inner class PowerUpEntity(
        x: Float,
        y: Float,
    ) : Entity(x, y) {
        private val width = entityWidth
        private val height = entityHeight

        private val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.LTGRAY
        }

        override fun updateAndRender(canvas: Canvas) {
            canvas.drawOval(RectF(x + (width / 4), y + (height / 4), x + (width * 3 / 4), y + (height * 3 / 4)), paint)
        }
    }

    inner class WallEntity(
        x: Float,
        y: Float,
    ) : Entity(x, y) {
        private val width = entityWidth
        private val height = entityHeight

        private val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.BLUE
        }

        override fun updateAndRender(canvas: Canvas) {
            canvas.drawRect(RectF(x, y, (x + width), (y + height)), paint)
        }
    }

    inner class FoodEntity(
        x: Float,
        y: Float,
    ) : Entity(x, y) {

        private val width = entityWidth
        private val height = entityHeight

        private val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.LTGRAY
        }

        private val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.0.toPx
            color = Color.LTGRAY
        }

        override fun updateAndRender(canvas: Canvas) {
            canvas.drawOval(RectF(x + (width * 4 / 10), y + (height * 4 / 10), x + (width * 6 / 10), y + (height * 6 / 10)), paint)
        }
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        color = Color.WHITE
    }

    private val boldTextPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    fun updateAndRender(canvas: Canvas) {
//        val pacman = entities.first()
//        entities.drop(1).forEach {
//            it.updateAndRender(canvas)
//        }
        entitiesMap.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, entity ->
                entity?.updateAndRender(canvas)
            }
        }
        pacman?.updateAndRender(canvas)
        canvas.drawText("Rect Pos: (${pacman?.x?.toInt()}, ${pacman?.y?.toInt()})", sceneWidth / 2, sceneHeight / 2, textPaint)
        canvas.drawText("Score: $score", 50f, sceneHeight - 50f, boldTextPaint)



        frameCount++
    }

    var pressedButtonDirection: Direction? = null

    fun onViewEvent(viewEvent: ViewEvent) {
        when (viewEvent) {
            ViewEvent.UP_ARROW_PRESSED -> pressedButtonDirection = Direction.UP
            ViewEvent.UP_ARROW_RELEASED -> pressedButtonDirection = null
            ViewEvent.DOWN_ARROW_PRESSED -> pressedButtonDirection = Direction.DOWN
            ViewEvent.DOWN_ARROW_RELEASED -> pressedButtonDirection = null
            ViewEvent.LEFT_ARROW_PRESSED -> pressedButtonDirection = Direction.LEFT
            ViewEvent.LEFT_ARROW_RELEASED -> pressedButtonDirection = null
            ViewEvent.RIGHT_ARROW_PRESSED -> pressedButtonDirection = Direction.RIGHT
            ViewEvent.RIGHT_ARROW_RELEASED -> pressedButtonDirection = null
        }
    }

    private fun removeEntity(entity: Entity) {
        val memX = (entity.x / entityWidth).toInt()
        val memY = (entity.y / entityHeight).toInt()

        entitiesMap[memY][memX] = null
    }

    private fun getMemXFromPos(x: Float): Int = (x / entityWidth).toInt()
    private fun getMemYFromPos(y: Float): Int = (y / entityHeight).toInt()

    private fun getEntityFromPos(x: Float, y: Float): Entity? {
        val memX = (x / entityWidth).toInt()
        val memY = (y / entityHeight).toInt()

        return entitiesMap[memY][memX]
    }

    fun sceneSizeChanged(w: Int, h: Int) {
        sceneWidth = w.toFloat()
        sceneHeight = h.toFloat()

        entities.clear()
        val rows = gameboard.split("\n")

        mapWidth = rows.first().length
        mapHeight = rows.size
        entityWidth = sceneWidth / mapWidth
        entityHeight = sceneHeight / mapHeight
        entitiesMap = Array<Array<Entity?>>(mapHeight) {
            Array(mapWidth) {
                null
            }
        }

        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                when (c) {
                    'P' -> {
                        val entity = PacmanEntity(x * entityWidth, y * entityHeight)
                        entities.add(0, entity)
                        entitiesMap[y][x] = entity
                        pacman = entity
                    }
                    'o' -> {
                        val entity = PowerUpEntity(x * entityWidth, y * entityHeight)
                        entities.add(entity)
                        entitiesMap[y][x] = entity
                    }
                    '.' -> {
                        val entity = FoodEntity(x * entityWidth, y * entityHeight)
                        entities.add(entity)
                        entitiesMap[y][x] = entity
                    }
                    '#' -> {
                        val entity = WallEntity(x * entityWidth, y * entityHeight)
                        entities.add(entity)
                        entitiesMap[y][x] = entity
                    }
                    else -> {}
                }
            }
        }
    }

    fun directionChanged(direction: Direction) {
        this.direction = direction
    }


    enum class ViewEvent {
        UP_ARROW_PRESSED,
        UP_ARROW_RELEASED,
        DOWN_ARROW_PRESSED,
        DOWN_ARROW_RELEASED,
        LEFT_ARROW_PRESSED,
        LEFT_ARROW_RELEASED,
        RIGHT_ARROW_PRESSED,
        RIGHT_ARROW_RELEASED,
    }

}