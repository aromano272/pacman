package com.andreromano.pacman

import android.graphics.*
import androidx.core.graphics.toRectF
import com.andreromano.pacman.extensions.scale
import com.andreromano.pacman.extensions.toPx

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
           P      #      #          
        ######.##.#      #.##.######
             #.##.########.##.#     
             #.##..........##.#     
             #.##.########.##.#     
        ######.##.########.##.######
        #............##............#
        #.####.#####.##.#####.####.#
        #.####.#####.##.#####.####.#
        #o..##................##..o#
        ###.##.##.########.##.##.###
        ###.##.##.########.##.##.###
        #......##....##....##......#
        ############################
    """.trimIndent()

    private var sceneWidth = 0
    private var sceneHeight = 0
    private var entityWidth = 0
    private var entityHeight = 0
    private var mapTileWidth = 0
    private var mapTileHeight = 0

    private var frameCount: Long = 0

//    private var direction: Direction = Direction.RIGHT

    private var score: Int = 0

    private var pacman: PacmanEntity? = null
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

    inner class PacmanEntity(
        screenPos: Vec2,
        tilePos: Vec2,
        width: Int,
        height: Int,
    ) : Entity(screenPos, tilePos, width, height) {

        val velocity = 5

        override fun updateAndRender(canvas: Canvas) {
            val pressedButtonDirection = pressedButtonDirection
            if (pressedButtonDirection != null) {
                var newX = x
                var newY = y
                when (pressedButtonDirection) {
                    Direction.UP -> newY = y - velocity
                    Direction.RIGHT -> newX = x + velocity
                    Direction.DOWN -> newY = y + velocity
                    Direction.LEFT -> newX = x - velocity
                }

                // check if out of bounds and wrap
                if (newX >= sceneWidth) {
                    newX -= sceneWidth
                }
                if (newX < 0) {
                    newX += sceneWidth
                }
                if (newY >= sceneHeight) {
                    newY -= sceneHeight
                }
                if (newY < 0) {
                    newY += sceneHeight
                }

                val testPos = Vec2(newX, newY)
                val testTLTile = getTilePosFromScreenPos(testPos)
                val testTLEntity = getEntity(testTLTile)

                val testTRTile = getTilePosFromScreenPos(testPos.copy(x = testPos.x + width - 1))
                val testTREntity = getEntity(testTRTile)

                val testBLTile = getTilePosFromScreenPos(testPos.copy(y = testPos.y + height - 1))
                val testBLEntity = getEntity(testBLTile)

                val testBRTile = getTilePosFromScreenPos(testPos.copy(x = testPos.x + width - 1, y = testPos.y + height - 1))
                val testBREntity = getEntity(testBRTile)

                // TODO: Check all entities between pacman and testEntity to prevent tunneling during high speed moves or low frame rate
                if (testTLEntity !is WallEntity && testTREntity !is WallEntity && testBLEntity !is WallEntity && testBREntity !is WallEntity) {
                    screenPos = testPos
                    tilePos = testTLTile


                    if (testTLEntity is FoodEntity) {
                        score += 100
                        removeEntity(testTLEntity)
                    }
                } else {

//                    val collidingWallEntity =
//                        (testTLEntity as? WallEntity) ?:
//                        (testTREntity as? WallEntity) ?:
//                        (testBLEntity as? WallEntity) ?:
//                        (testBREntity as? WallEntity)!!


                    when (pressedButtonDirection) {
                        Direction.UP -> {
                            val wall = (testTLEntity as? WallEntity) ?: (testTREntity as? WallEntity)!!
                            val newTileY = wall.tileY + 1
                            val newScreenY = getScreenYFromTileY(newTileY)
                            y = newScreenY
                            tilePos = getTilePosFromScreenPos(screenPos)
                        }
                        Direction.RIGHT -> {
                            val wall = (testTREntity as? WallEntity) ?: (testBREntity as? WallEntity)!!
                            val newTileX = wall.tileX - 1
                            val newScreenX = getScreenXFromTileX(newTileX)
                            x = newScreenX
                            tilePos = getTilePosFromScreenPos(screenPos)
                        }
                        Direction.DOWN -> {
                            val wall = (testBLEntity as? WallEntity) ?: (testBREntity as? WallEntity)!!
                            val newTileY = wall.tileY - 1
                            val newScreenY = getScreenYFromTileY(newTileY)
                            y = newScreenY
                            tilePos = getTilePosFromScreenPos(screenPos)
                        }
                        Direction.LEFT -> {
                            val wall = (testTLEntity as? WallEntity) ?: (testBLEntity as? WallEntity)!!
                            val newTileX = wall.tileX + 1
                            val newScreenX = getScreenXFromTileX(newTileX)
                            x = newScreenX
                            tilePos = getTilePosFromScreenPos(screenPos)
                        }
                    }

//                    when (pressedButtonDirection) {
//                        Direction.UP -> TODO()
//                        Direction.RIGHT -> TODO()
//                        Direction.DOWN -> TODO()
//                        Direction.LEFT -> TODO()
//                    }
//
//
//
//
//                    val testScreenPos = getScreenPosFromTilePos(testTile)
//
//                    when (pressedButtonDirection) {
//                        Direction.UP -> y = testScreenPos.y + entityHeight
//                        Direction.RIGHT -> x = testScreenPos.x - entityWidth
//                        Direction.DOWN -> y = testScreenPos.y - entityHeight
//                        Direction.LEFT -> x = testScreenPos.x + entityWidth
//                    }
                }
            }

            val right = width + x
            val bottom = height + y

            val rect = screenRect.toRectF().scale(0.75f)
            canvas.drawOval(rect, yellowPaint)
            // enables wrapping around the scene
            if (right > sceneWidth) canvas.drawOval(Rect(right - sceneWidth - width, y, right - sceneWidth, bottom).toRectF().scale(0.75f), yellowPaint)
            if (bottom > sceneHeight) canvas.drawOval(Rect(x, bottom - sceneHeight - height, right, bottom - sceneHeight).toRectF().scale(0.75f), yellowPaint)
            if (right > sceneWidth && bottom > sceneHeight) canvas.drawOval(Rect(right - sceneWidth - width, bottom - sceneHeight - height, right - sceneWidth, bottom - sceneHeight).toRectF().scale(0.75f), yellowPaint)

            // test entity grid bounding box
            val testX = tileX * entityWidth
            val testY = tileY * entityHeight
            canvas.drawRect(Rect(testX, testY, testX + entityWidth, testY + entityHeight), redStrokePaint)
        }
    }

    private fun Vec2.correctOutOfBounds(): Vec2 {
        var x = this.x
        var y = this.y
        // check if out of bounds and wrap
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

        return Vec2(x, y)
    }


    inner class PowerUpEntity(
        screenPos: Vec2,
        tilePos: Vec2,
        width: Int,
        height: Int,
    ) : Entity(screenPos, tilePos, width, height) {

        private val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.LTGRAY
        }

        override fun updateAndRender(canvas: Canvas) {
            canvas.drawOval(Rect(x, y, x + width, y + height).toRectF().scale(0.5f), paint)
        }
    }

    inner class WallEntity(
        screenPos: Vec2,
        tilePos: Vec2,
        width: Int,
        height: Int,
    ) : Entity(screenPos, tilePos, width, height) {

        private val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.BLUE
        }

        override fun updateAndRender(canvas: Canvas) {
            canvas.drawRect(Rect(x, y, x + width, y + height), paint)
        }
    }

    inner class FoodEntity(
        screenPos: Vec2,
        tilePos: Vec2,
        width: Int,
        height: Int,
    ) : Entity(screenPos, tilePos, width, height) {

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
            canvas.drawOval(RectF(x + (width.toFloat() * 4 / 10), y + (height.toFloat() * 4 / 10), x + (width.toFloat() * 6 / 10), y + (height.toFloat() * 6 / 10)), paint)
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
        entitiesMap.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, entity ->
                entity?.updateAndRender(canvas)
            }
        }
        pacman?.updateAndRender(canvas)
        canvas.drawText("Rect Pos: (${pacman?.x}, ${pacman?.y})", sceneWidth / 2f, sceneHeight / 2f, textPaint)
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
        entitiesMap[entity.tileY][entity.tileX] = null
    }

    private fun getScreenXFromTileX(tileX: Int): Int = tileX * entityWidth
    private fun getScreenYFromTileY(tileY: Int): Int = tileY * entityHeight

    private fun getTileXFromScreenX(screenX: Int): Int = screenX / entityWidth
    private fun getTileYFromScreenY(screenY: Int): Int = screenY / entityHeight

    private fun getScreenPosFromTilePos(tilePos: Vec2): Vec2 {
        val tileX = getScreenXFromTileX(tilePos.x)
        val tileY = getScreenYFromTileY(tilePos.y)

        return Vec2(tileX, tileY)
    }

    private fun getTilePosFromScreenPos(screenPos: Vec2): Vec2 {
        val tileX = getTileXFromScreenX(screenPos.x)
        val tileY = getTileYFromScreenY(screenPos.y)

        return Vec2(tileX, tileY)
    }

    private fun getTilePosFromScreenPos(x: Int, y: Int): Vec2 {
        val tileX = getTileXFromScreenX(x)
        val tileY = getTileYFromScreenY(y)

        return Vec2(tileX, tileY)
    }

    private fun getEntity(tilePos: Vec2): Entity? = entitiesMap[tilePos.y][tilePos.x]

    fun sceneSizeChanged(w: Int, h: Int) {
        val rows = gameboard.split("\n")

        mapTileWidth = rows.first().length
        mapTileHeight = rows.size
        entityWidth = w / mapTileWidth
        entityHeight = h / mapTileHeight

        // TODO: Pad and center gameboard
        val remainderWidth = w % mapTileWidth
        val remainderHeight = h % mapTileHeight

        sceneWidth = w - remainderWidth
        sceneHeight = h - remainderHeight

        entitiesMap = Array<Array<Entity?>>(mapTileHeight) {
            Array(mapTileWidth) {
                null
            }
        }

        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                val tilePos = Vec2(x, y)
                val screenPos = getScreenPosFromTilePos(tilePos)
                when (c) {
                    'P' -> {
                        val entity = PacmanEntity(screenPos, tilePos, entityWidth, entityHeight)
                        entitiesMap[y][x] = entity
                        pacman = entity
                    }
                    'o' -> {
                        val entity = PowerUpEntity(screenPos, tilePos, entityWidth, entityHeight)
                        entitiesMap[y][x] = entity
                    }
                    '.' -> {
                        val entity = FoodEntity(screenPos, tilePos, entityWidth, entityHeight)
                        entitiesMap[y][x] = entity
                    }
                    '#' -> {
                        val entity = WallEntity(screenPos, tilePos, entityWidth, entityHeight)
                        entitiesMap[y][x] = entity
                    }
                    else -> {}
                }
            }
        }
    }

    fun directionChanged(direction: Direction) {
//        this.direction = direction
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