package com.andreromano.pacman

import android.graphics.*
import androidx.core.graphics.toRectF
import com.andreromano.pacman.entities.FoodEntity
import com.andreromano.pacman.entities.PowerUpEntity
import com.andreromano.pacman.entities.WallEntity
import com.andreromano.pacman.extensions.*
import kotlin.random.Random

class Game {

    enum class Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT;

        fun isHorizontal(): Boolean = when (this) {
            UP,
            DOWN -> false
            RIGHT,
            LEFT -> true
        }

        fun isVertical() = !isHorizontal()
    }

    private var currentLevel: Level? = Level.ONE

    private var screenWidth = 0
    private var screenHeight = 0
    private var sceneWidth = 0
    private var sceneHeight = 0
    private var entityWidth = 0
    private var entityHeight = 0
    private var mapTileWidth = 0
    private var mapTileHeight = 0

    private var frameCount: Long = 0

    private var score: Int = 0

    private var pacman: PacmanEntity? = null
    private var entitiesMap: Array<Array<Entity?>> = Array<Array<Entity?>>(mapTileHeight) {
        Array(mapTileWidth) {
            null
        }
    }

    private val redStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 4f
    }

    private val greenStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 4f
    }

    private val greenPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val blackPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val bluePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    private val whitePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val yellowPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
    }

    inner class PacmanEntity(
        x: Int,
        y: Int,
        tileX: Int,
        tileY: Int,
        width: Int,
        height: Int
    ) : MovingEntity(x, y, tileX, tileY, width, height) {
        override fun render(canvas: Canvas) {
            canvas.drawOval(hitbox.toRectF(), yellowPaint)
            canvas.drawRect(hitbox.toRectF(), greenStrokePaint)
        }

        override fun onHitDetected(hitEntity: Entity) {
            super.onHitDetected(hitEntity)
            if (hitEntity is FoodEntity) {
                score += 100
                removeEntity(hitEntity)
            } else if (hitEntity is PowerUpEntity) {
                score += 1001
                removeEntity(hitEntity)
            } else if (hitEntity is GhostEntity) {
                defer { restartLevel() }
            }
        }
    }

    inner class GhostEntity(
        x: Int,
        y: Int,
        tileX: Int,
        tileY: Int,
        width: Int,
        height: Int
    ) : MovingEntity(x, y, tileX, tileY, width, height) {

        init {
            changeToRandomDirection()
        }

        private var isBlue = true

        override fun update(canvas: Canvas) {
            val oldX = x
            val oldY = y
            super.update(canvas)
            // has stoppped
            if (oldX == x && oldY == y) {
                changeToRandomDirection()
            }
            println("$frameCount $direction")
        }

        override fun render(canvas: Canvas) {
            val blinkFrequency = 20
            if (frameCount % blinkFrequency == 0L) {
                isBlue = !isBlue
            }
            val paint = if (isBlue) bluePaint else whitePaint

            canvas.drawOval(hitbox.toRectF(), paint)
            canvas.drawRect(hitbox.copy(top = hitbox.top + height / 2), paint)
        }

        private fun changeToRandomDirection() {
            queuedDirection = Direction.values()[Random.nextInt(Direction.values().size)]
        }
    }

    abstract inner class MovingEntity(
        x: Int,
        y: Int,
        tileX: Int,
        tileY: Int,
        width: Int,
        height: Int
    ) : Entity(x, y, tileX, tileY, width, height) {

        lateinit var direction: Direction
        lateinit var queuedDirection: Direction
        val velocity = 3

        private fun Entity.postMovePosition(velocity: Int, direction: Direction): Position {
            val x = moveXInDirection(x, velocity, direction)
            val y = moveYInDirection(y, velocity, direction)
            return Position(
                x = moveXInDirection(x, velocity, direction),
                y = moveYInDirection(y, velocity, direction),
                tileX = getTileXFromScreenX(x),
                tileY = getTileYFromScreenY(y),
            )
        }

        private fun moveXInDirection(x: Int, velocity: Int, direction: Direction): Int = when (direction) {
            Direction.UP -> x
            Direction.RIGHT -> x + velocity
            Direction.DOWN -> x
            Direction.LEFT -> x - velocity
        }

        private fun moveYInDirection(y: Int, velocity: Int, direction: Direction): Int = when (direction) {
            Direction.UP -> y - velocity
            Direction.RIGHT -> y
            Direction.DOWN -> y + velocity
            Direction.LEFT -> y
        }

        // Negative values == distance until center, Positive values == distance past center
        private fun distancePastCenterOfTile(newX: Int, newY: Int, queuedDirection: Direction): Int {
            val newTileX = getTileXFromScreenX(newX)
            val centerOfNewTileX = getScreenXFromTileX(newTileX)
            val newTileY = getTileYFromScreenY(newY)
            val centerOfNewTileY = getScreenYFromTileY(newTileY)
            return when (queuedDirection) {
                Direction.UP -> {
                    centerOfNewTileY - newY
                }
                Direction.RIGHT -> {
                    newX - centerOfNewTileX
                }
                Direction.DOWN -> {
                    newY - centerOfNewTileY
                }
                Direction.LEFT -> {
                    centerOfNewTileX - newX
                }
            }
        }

        override fun update(canvas: Canvas) {
            if (!::direction.isInitialized && ::queuedDirection.isInitialized) direction = queuedDirection
            if (!::queuedDirection.isInitialized) return
            var velocityLeft = velocity

            // direction
            val postMovePos = postMovePosition(velocity, direction)

            if (direction != queuedDirection) {
                if (areDirectionsOpposite(direction, queuedDirection)) {
                    direction = queuedDirection
                } else {
                    val partialTileX = getPartialTileXFromScreenX(x, tileX, width)
                    val postMovePartialTileX = getPartialTileXFromScreenX(postMovePos.x, postMovePos.tileX, width)
                    val partialTileY = getPartialTileYFromScreenY(y, tileY, height)
                    val postMovePartialTileY = getPartialTileYFromScreenY(postMovePos.y, postMovePos.tileY, height)

                    // [--0][x0-][---] ==> [---][-0x][0--]
                    // [--0][x0-][---] <== [---][-0x][0--]
                    if (
                        (direction.isHorizontal() && (isOnCenterOfTile() || partialTileX != postMovePartialTileX)) ||
                            (direction.isVertical() && (isOnCenterOfTile() || partialTileY != postMovePartialTileY))
                    ) {
                        val testEntity = getAdjacentEntity(currentPos(), queuedDirection)

                        if (testEntity is WallEntity) {
                            // no op, do not change direction
                        } else {
                            // finish moving until center cell
                            val distancePastCenterOfTile = distancePastCenterOfTile(postMovePos.x, postMovePos.y, direction)
                            if (distancePastCenterOfTile >= 0) {
                                direction = queuedDirection
                                velocityLeft -= distancePastCenterOfTile

                                x = getScreenXFromTileX(tileX)
                                y = getScreenYFromTileY(tileY)
                            } else {
                                // no op, do not change direction
                            }
                        }
                    }
                }
            }

            val oldX = x
            val oldY = y
            when (direction) {
                Direction.UP -> y -= velocityLeft
                Direction.RIGHT -> x += velocityLeft
                Direction.DOWN -> y += velocityLeft
                Direction.LEFT -> x -= velocityLeft
            }

            val testEntity = when (direction) {
                Direction.UP -> getEntity(tileX, tileY - 1)
                Direction.RIGHT -> getEntity(tileX + 1, tileY)
                Direction.DOWN -> getEntity(tileX, tileY + 1)
                Direction.LEFT -> getEntity(tileX - 1, tileY)
            }
            if (testEntity != null && testEntity.hitbox.intersect(this.hitbox)) onHitDetected(testEntity)
            if (testEntity is WallEntity && testEntity.hitbox.intersect(this.hitbox)) {
                x = getScreenXFromTileX(tileX)
                y = getScreenYFromTileY(tileY)

                canvas.drawRect(testEntity.hitbox, redStrokePaint)
            }

            entitiesMap[tileY][tileX] = null
            tileX = getTileXFromScreenX(x)
            tileY = getTileYFromScreenY(y)
            entitiesMap[tileY][tileX] = this

            // queued direction
            // out of bounds wrapping
            // collision
        }

        open fun onHitDetected(hitEntity: Entity) {
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

    private val deferred: MutableList<() -> Unit> = mutableListOf()

    fun updateAndRender(canvas: Canvas) {
        entitiesMap.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, entity ->
                if (entity !is MovingEntity) {
                    entity?.update(canvas)
                    entity?.render(canvas)
                }
            }
        }
        entitiesMap.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, entity ->
                if (entity is MovingEntity) {
                    entity.update(canvas)
                    entity.render(canvas)
                }
            }
        }

        canvas.drawText("Rect Pos: (${pacman?.x}, ${pacman?.y})", sceneWidth / 2f, sceneHeight / 2f, textPaint)
        canvas.drawText("Score: $score", 50f, sceneHeight - 50f, boldTextPaint)

        // clear padding
        canvas.drawRect(Rect(sceneWidth, 0, screenWidth, screenHeight), blackPaint)
        canvas.drawRect(Rect(0, sceneHeight, screenWidth, screenHeight), blackPaint)

        deferred.forEach {
            it()
        }
        deferred.clear()

        frameCount++
    }

    fun defer(runnable: () -> Unit) {
        deferred += runnable
    }

    fun onViewEvent(viewEvent: ViewEvent) {
        when (viewEvent) {
            ViewEvent.UP_ARROW_PRESSED -> {}
            ViewEvent.UP_ARROW_RELEASED -> pacman?.queuedDirection = Direction.UP
            ViewEvent.DOWN_ARROW_PRESSED -> {}
            ViewEvent.DOWN_ARROW_RELEASED -> pacman?.queuedDirection = Direction.DOWN
            ViewEvent.LEFT_ARROW_PRESSED -> {}
            ViewEvent.LEFT_ARROW_RELEASED -> pacman?.queuedDirection = Direction.LEFT
            ViewEvent.RIGHT_ARROW_PRESSED -> {}
            ViewEvent.RIGHT_ARROW_RELEASED -> pacman?.queuedDirection = Direction.RIGHT
            ViewEvent.RESTART_CLICKED -> restartLevel()
        }.exhaustive
    }

    private fun removeEntity(entity: Entity) {
        entitiesMap[entity.tileY][entity.tileX] = null
    }

    private fun getScreenXFromTileX(tileX: Int): Int = tileX * entityWidth + entityWidth / 2
    private fun getScreenYFromTileY(tileY: Int): Int = tileY * entityHeight + entityHeight / 2

    private fun getTileXFromScreenX(screenX: Int): Int = screenX / entityWidth
    private fun getTileYFromScreenY(screenY: Int): Int = screenY / entityHeight

    private fun Entity.isOnCenterOfTile(): Boolean = getPartialTileXFromScreenX() == null && getPartialTileYFromScreenY() == null

    private fun Entity.getPartialTileXFromScreenX(): Int? = getPartialTileXFromScreenX(x, tileX, width)
    private fun getPartialTileXFromScreenX(x: Int, tileX: Int, width: Int): Int? {
        val centerOfTile = tileX * width + width / 2
        return when {
            // [---][0x0][---]
            x == centerOfTile -> null
            // [--0][x0-][---]
            x < centerOfTile -> tileX - 1
            // [---][-0x][0--]
            else -> tileX + 1
        }
    }

    private fun Entity.getPartialTileYFromScreenY(): Int? = getPartialTileYFromScreenY(y, tileY, height)
    private fun getPartialTileYFromScreenY(y: Int, tileY: Int, height: Int): Int? {
        val centerOfTile = tileY * height + height / 2
        return when {
            // [---][0x0][---]
            y == centerOfTile -> null
            // [--0][x0-][---]
            y < centerOfTile -> tileY - 1
            // [---][-0x][0--]
            else -> tileY + 1
        }
    }


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

    private fun areDirectionsOpposite(a: Direction, b: Direction) = when (a) {
        Direction.UP -> b == Direction.DOWN
        Direction.RIGHT -> b == Direction.LEFT
        Direction.DOWN -> b == Direction.UP
        Direction.LEFT -> b == Direction.RIGHT
    }

    private fun getEntity(tilePos: Vec2): Entity? = entitiesMap[tilePos.y][tilePos.x]

    private fun getEntity(tileX: Int, tileY: Int): Entity? = entitiesMap[tileY][tileX]

    private fun getEntity(pos: Position): Entity? = entitiesMap[pos.tileY][pos.tileX]

    private fun getAdjacentEntity(currPos: Position, direction: Direction): Entity? = getAdjacentEntity(currPos.tileX, currPos.tileY, direction)
    private fun getAdjacentEntity(tileX: Int, tileY: Int, direction: Direction): Entity? = when (direction) {
        Direction.UP -> getEntity(tileX, tileY - 1)
        Direction.RIGHT -> getEntity(tileX + 1, tileY)
        Direction.DOWN -> getEntity(tileX, tileY + 1)
        Direction.LEFT -> getEntity(tileX - 1, tileY)
    }

    fun sceneSizeChanged(w: Int, h: Int) {
        screenWidth = w
        screenHeight = h

        currentLevel?.let { loadLevel(it) }
    }

    fun restartLevel() {
        currentLevel?.let { loadLevel(it) }
    }

    fun loadLevel(level: Level) {
        score = 0
        val rows = level.gameboard.split("\n")

        mapTileWidth = rows.first().length
        mapTileHeight = rows.size
        entityWidth = screenWidth / mapTileWidth
        entityHeight = screenHeight / mapTileHeight

        // Make size divisible by 2 so we don't issues with center
        entityWidth -= entityWidth % 2
        entityHeight -= entityHeight % 2

        // TODO: Pad and center gameboard
        val remainderWidth = screenWidth % mapTileWidth
        val remainderHeight = screenHeight % mapTileHeight

        sceneWidth = screenWidth - remainderWidth
        sceneHeight = screenHeight - remainderHeight

        entitiesMap = Array<Array<Entity?>>(mapTileHeight) {
            Array(mapTileWidth) {
                null
            }
        }

        rows.forEachIndexed { tileY, row ->
            row.forEachIndexed { tileX, c ->
                val x = getScreenXFromTileX(tileX)
                val y = getScreenYFromTileY(tileY)
                when (c) {
                    'P' -> {
                        val entity = PacmanEntity(x, y, tileX, tileY, entityWidth, entityHeight)
                        entitiesMap[tileY][tileX] = entity
                        pacman = entity
                    }
                    'G' -> {
                        val entity = GhostEntity(x, y, tileX, tileY, entityWidth, entityHeight)
                        entitiesMap[tileY][tileX] = entity
                    }
                    'o' -> {
                        val entity = PowerUpEntity(x, y, tileX, tileY, entityWidth, entityHeight)
                        entitiesMap[tileY][tileX] = entity
                    }
                    '.' -> {
                        val entity = FoodEntity(x, y, tileX, tileY, entityWidth, entityHeight)
                        entitiesMap[tileY][tileX] = entity
                    }
                    '#' -> {
                        val entity = WallEntity(x, y, tileX, tileY, entityWidth, entityHeight)
                        entitiesMap[tileY][tileX] = entity
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
        RESTART_CLICKED,
    }

}