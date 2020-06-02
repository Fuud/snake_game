package ru.codebattle.client.newgame

import ru.codebattle.client.api.*
import ru.codebattle.client.api.BoardElement.*
import ru.codebattle.client.api.Direction.*
import ru.codebattle.client.api.Prices.EVIL_DURATION
import ru.codebattle.client.api.Prices.S.*
import ru.codebattle.client.api.Prices.price
import kotlin.math.abs
import kotlin.math.min

fun Game.findPath(ourSnake: Snake, excludeAtFirstMove: Set<Direction>, doNotStepBack: Boolean): SnakeAction? {
    val startTime = System.currentTimeMillis()
    val MAX_ALLOWED_TIME = 700

    val MAX_DEPTH = min(15, 300 - tick)
    val FURY_APPLE_STONE = listOf(FURY_PILL, APPLE, STONE)

    class ComparableList(val list: List<Int>) : Comparable<ComparableList> {
        override fun compareTo(other: ComparableList): Int {
            when {
                this.list.size > other.list.size -> return 1
                this.list.size < other.list.size -> return -1
                else -> {
                    this.list.forEachIndexed { index, th ->
                        if (th < other.list[index]) {
                            return -1
                        } else if (th > other.list[index]) {
                            return 1
                        }
                    }
                    return 0
                }
            }
        }
    }

    class PointWithScore(
            val point: BoardPoint,
            val distance: Int,
            val prevPathCost: Double,
            val cost: Int,
            val accumulatedCost: Double,
            val direction: Direction?,
            val prev: PointWithScore?,
            val next: MutableList<PointWithScore> = mutableListOf(),
            val betterNextFromOtherPath: MutableList<PointWithScore> = mutableListOf(),
            val snake: Snake,
            val visitedPoints: Set<BoardPoint>,
            val itemsTaken: Set<BoardPoint>
    ) : Comparable<PointWithScore> {
        val elem: BoardElement = board[point]!!

        private val itemsTakenComparable = ComparableList(itemsTaken.toList().map { it.x * board.size + it.y }.sorted())

        override fun compareTo(other: PointWithScore): Int {
            return when {
                distance != other.distance -> distance - other.distance
                point.x != other.point.x -> point.x - other.point.x
                point.y != other.point.y -> point.y - other.point.y
                itemsTaken.size != other.itemsTaken.size -> itemsTaken.size - other.itemsTaken.size
                else -> itemsTakenComparable.compareTo(other.itemsTakenComparable)
            }
        }

        override fun toString(): String {
            val track = track().zipWithNext().map { (cur, next) -> "${cur.directionTo(next)}$next${board[next]!!.name}" }
            return "$point ${elem.name} $accumulatedCost $track"
        }

        fun track(): List<BoardPoint> = (prev?.track() ?: emptyList()) + this.point
        fun initialDirection(): Direction? = prev?.initialDirection() ?: direction
    }

    // pessimistic approach
    val enemySnakes = (snakes - ourSnake).map {
        val head = it.body[0]
        val shouldAddDummyTailSegment = listOf(DOWN, UP, LEFT, RIGHT).map { head.shift(it) }.any { board[it] == APPLE }
        val shouldAddFury = listOf(DOWN, UP, LEFT, RIGHT).map { head.shift(it) }.any { board[it] == FURY_PILL }
        when {
            shouldAddDummyTailSegment && shouldAddFury -> it.copy(body = it.body + it.body.last(), evilTickRemind = it.evilTickRemind + EVIL_DURATION)
            shouldAddDummyTailSegment -> it.copy(body = it.body + it.body.last())
            shouldAddFury -> it.copy(evilTickRemind = it.evilTickRemind + EVIL_DURATION)
            else -> it
        }
    }

    val front = sortedSetOf<PointWithScore>()

    front.add(PointWithScore(
            point = ourSnake.body[0],
            distance = 0,
            prevPathCost = 0.0,
            cost = 0,
            accumulatedCost = 0.0,
            direction = null,
            prev = null,
            snake = ourSnake,
            visitedPoints = emptySet(),
            itemsTaken = emptySet()
    ))

    class PointWithItems(val point: BoardPoint, val taken: Set<BoardPoint>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PointWithItems

            if (point != other.point) return false
            if (!doNotStepBack) {
                if (taken != other.taken) return false
            }
            return true
        }

        override fun hashCode(): Int {
            var result = point.hashCode()
            if (!doNotStepBack) {
                result = 31 * result + taken.hashCode()
            }
            return result
        }
    }

    val visited: MutableMap<PointWithItems, PointWithScore> = mutableMapOf()

    while (front.isNotEmpty() && System.currentTimeMillis() - startTime < MAX_ALLOWED_TIME) {

        val current = front.pollFirst()!!

        if (current.distance >= MAX_DEPTH) {
            continue
        }

        val forbiddenDirection = if (current.distance == 0) {
            excludeAtFirstMove + ourSnake.lastMove.opposite()
        } else {
            listOf(current.direction!!.opposite())
        }

        listOf(DOWN, UP, LEFT, RIGHT).filterNot { it in forbiddenDirection }
                .forEach { direction ->
                    val newPoint = current.point.shift(direction)
                    if (newPoint.isOutOfBoard(board.size)) {
                        return@forEach
                    }

                    val newDistance = current.distance + 1
                    val (score, willSurvive, newSnake) = cost(current.visitedPoints, newPoint, newDistance, current.snake, enemySnakes)
                    val visitedPoints = if (current.visitedPoints.contains(newPoint)) {
                        current.visitedPoints
                    } else {
                        current.visitedPoints + newPoint
                    }

                    val takenPoints = if (board[newPoint] !in FURY_APPLE_STONE || current.itemsTaken.contains(newPoint)) {
                        current.itemsTaken
                    } else {
                        current.itemsTaken + newPoint
                    }

                    val pointWithItems = PointWithItems(
                            newPoint,
                            takenPoints
                    )

                    val newScore = PointWithScore(
                            point = newPoint,
                            distance = newDistance,
                            prevPathCost = current.accumulatedCost,
                            direction = direction,
                            cost = score,
                            accumulatedCost = current.accumulatedCost + score.toDouble() / newDistance,
                            prev = current,
                            snake = newSnake,
                            visitedPoints = visitedPoints,
                            itemsTaken = pointWithItems.taken
                    )
                    current.next.add(newScore)


                    val prevVisit = visited[pointWithItems]
                    if (prevVisit == null || prevVisit > newScore) {
                        visited[pointWithItems] = newScore
                        prevVisit?.prev?.betterNextFromOtherPath?.add(newScore)
                        if (willSurvive) { // no future path if we are dead
                            front += newScore
                        }
                    } else {
                        current.betterNextFromOtherPath.add(prevVisit)
                    }
                }
    }

    val finished = front.isEmpty()
    val searchBestFrom = if (finished) {
        val willSurvive = visited.values.any { it.distance == MAX_DEPTH }
        if (willSurvive) {
            visited.values.filter { it.distance == MAX_DEPTH }
        } else {
            visited.values
        }
    } else {
        val lastDistance = front.pollFirst().distance
        visited.values.filter { it.distance == lastDistance || it.distance == lastDistance - 1 }
    }
    val theBest = searchBestFrom
            .maxBy { it.accumulatedCost }.apply {
                println(this)
                println()
            }
    return theBest?.let { best ->
        val initialDirection = best.initialDirection()
        if (initialDirection == null){
            return@let null
        }
        if (ourSnake.evilTickRemind > 1 && ourSnake.stoneCount > 0 &&
                best.track().size > 1 && best.track()[1] == ourSnake.tail) {
            SnakeAction(true, initialDirection)
        } else {
            SnakeAction(false, initialDirection)
        }
    }
}

data class Cost(val score: Int, val willSurvive: Boolean, val newSnake: Snake)

fun deadCost(score: Prices.S, snake: Snake) = Cost(price(score), willSurvive = false, newSnake = snake)
fun deadCost(score: Int, snake: Snake) = Cost(score, willSurvive = false, newSnake = snake)
fun aliveCost(score: Prices.S, snake: Snake) = Cost(price(score), willSurvive = true, newSnake = snake)
fun aliveMinCost(score1: Prices.S, score2: Prices.S, snake: Snake) = Cost(Math.min(price(score1), price(score2))
        , willSurvive = true, newSnake = snake)

fun aliveMaxCost(score1: Prices.S, score2: Prices.S, snake: Snake) = Cost(Math.max(price(score1), price(score2))
        , willSurvive = true, newSnake = snake)

fun aliveCost(score: Int, snake: Snake) = Cost(score, willSurvive = true, newSnake = snake)

fun Game.cost(alreadyVisitedPoints: Set<BoardPoint>, newPoint: BoardPoint, distance: Int, ourSnake: Snake, enemySnakes: List<Snake>): Cost {
    if (alreadyVisitedPoints.contains(newPoint)) {
        return aliveCost(0, ourSnake.move(newPoint))
    }
    val element = this.board[newPoint] ?: return deadCost(SUICIDE_COST, ourSnake.move(newPoint))
    return when (element) {
        GOLD -> aliveCost(GOLD_COST, ourSnake.move(newPoint))
        APPLE -> {
            // если мы не злы
            if (ourSnake.evilTickRemind == 0) {
                // злой противник у пузика
                val fear = enemySnakes.filter { snake -> snake.evilTickRemind > distance }
                        .map { snake -> snake.head }
                        .any { head -> ourSnake.body.any() { p -> Math.abs(p.x - head.x) + Math.abs(p.y - head.y) < 3 } }

                if (fear) {
                    // из таблетки или яблока таблетка важней
                    return aliveMinCost(APPLE_COST, PILL_COST, ourSnake.move(newPoint, reduceTailBy = -1))
                }
            }
            aliveCost(APPLE_COST, ourSnake.move(newPoint, reduceTailBy = -1))
        } //todo: should be adjusted
        FURY_PILL -> {
            // если мы не злы
            if (ourSnake.evilTickRemind == 0) {
                // злой противник у пузика
                val fear = enemySnakes.filter { snake -> snake.evilTickRemind > distance }
                        .map { snake -> snake.head }
                        .any { head -> ourSnake.body.any() { p -> abs(p.x - head.x) + abs(p.y - head.y) < 3 } }

                if (fear) {
                    // из таблетки или яблока таблетка важней
                    return aliveMaxCost(APPLE_COST, PILL_COST, ourSnake.move(newPoint, evilTickDiff = EVIL_DURATION))
                }
            }

            aliveCost(PILL_COST, ourSnake.move(newPoint, evilTickDiff = EVIL_DURATION))
        } //todo
        WALL, START_FLOOR -> deadCost(SUICIDE_COST, ourSnake.move(newPoint))
        NONE -> aliveCost(NONE_COST, ourSnake.move(newPoint))
        STONE -> {
            if (ourSnake.evilTickRemind > 1) {
                aliveCost(PILL_COST, ourSnake.move(newPoint, stoneDiff = 1))
            } else {
                if (ourSnake.body.size >= 5) {
                    aliveCost(price(STONE_COST) + 3 * price(OUR_SNAKE_SEGMENT_COST), ourSnake.move(newPoint, reduceTailBy = 3, stoneDiff = 1)) //todo: check other snakes length
                } else {
                    deadCost(price(SUICIDE_COST) + price(STONE_COST), ourSnake.move(newPoint))
                }
            }
        }
        else -> {
            val posInOur = ourSnake.body.indexOf(newPoint)
            if (posInOur >= 0) {
                // при расчёте длины добавляем к номеру сегмента 1.
                val cutTailLength = (ourSnake.body.size - posInOur - 1)
                return if (cutTailLength < 1) {
                    if (cutTailLength == 0 && ourSnake.evilTickRemind > 1 && ourSnake.stoneCount > 0) {
                        aliveCost(STONE_COST, ourSnake.move(newPoint))
                    } else {
                        aliveCost(0, ourSnake.move(newPoint))
                    }
                } else {
                    aliveCost(price(OUR_SNAKE_SEGMENT_COST) * cutTailLength, ourSnake.move(newPoint, reduceTailBy = cutTailLength))
                }
            } else {
                val enemy = enemySnakes.singleOrNull { it.body.contains(newPoint) }
                if (enemy == null) {
                    //dead enemy
                    return aliveCost(0, ourSnake.move(newPoint))
                }
                val pos = enemy.body.indexOf(newPoint)
                if (enemy.body.size - pos <= distance) {
                    return aliveCost(0, ourSnake.move(newPoint))
                } else {
                    val weWillBeEvil = ourSnake.evilTickRemind > 1
                    val enemyWillBeEvil = enemy.evilTickRemind > distance
                    val longToLive = ourSnake.body.size - enemy.body.size >= 2
                    val enemyLongToLive = ourSnake.body.size - enemy.body.size <= -2
                    if (distance + pos == 1) { //neck
                        if (weWillBeEvil && !enemyWillBeEvil) {
                            return aliveCost(enemy.body.size * price(ENEMY_SEGMENT_COST), ourSnake.move(newPoint)) //todo may be bonus for kill
                        }
                        if (weWillBeEvil && enemyWillBeEvil && longToLive) {
                            return aliveCost(enemy.body.size * price(ENEMY_SEGMENT_COST), ourSnake.move(newPoint, reduceTailBy = ourSnake.body.size - enemy.body.size))
                        }

                        if (!weWillBeEvil && !enemyWillBeEvil && longToLive) {
                            return aliveCost(enemy.body.size * price(ENEMY_SEGMENT_COST), ourSnake.move(newPoint, reduceTailBy = ourSnake.body.size - enemy.body.size))
                        }
                        return if (enemyLongToLive) {
                            deadCost(KILLED_BY_ENEMY_COST, ourSnake.move(newPoint))
                        } else {
                            deadCost(price(KILLED_BY_ENEMY_COST) + enemy.body.size * price(ENEMY_SEGMENT_COST), ourSnake.move(newPoint))
                        }
                    } else {
                        return if (weWillBeEvil) {
                            aliveCost(((enemy.body.size - pos) - distance) * price(ENEMY_SEGMENT_COST), ourSnake.move(newPoint))
                        } else {
                            deadCost(KILLED_BY_ENEMY_COST, ourSnake.move(newPoint))
                        }
                    }
                }
            }
        }
    }
}
