package ru.codebattle.client.newgame

import ru.codebattle.client.api.BoardElement.*
import ru.codebattle.client.api.BoardPoint
import ru.codebattle.client.api.Direction
import ru.codebattle.client.api.Prices


fun findSnakes(board: Board): List<Snake> {
    return board.mapEachElement { point, elem ->
        when (elem) {
            in listOf(TAIL_END_DOWN, ENEMY_TAIL_END_DOWN) -> parseSnakeFromTail(board, point, Direction.UP)
            in listOf(TAIL_END_UP, ENEMY_TAIL_END_UP) -> parseSnakeFromTail(board, point, Direction.DOWN)
            in listOf(TAIL_END_LEFT, ENEMY_TAIL_END_LEFT) -> parseSnakeFromTail(board, point, Direction.RIGHT)
            in listOf(TAIL_END_RIGHT, ENEMY_TAIL_END_RIGHT) -> parseSnakeFromTail(board, point, Direction.LEFT)
            else -> null
        }
    }.filterNotNull()
}

private fun parseSnakeFromTail(board: Board, point: BoardPoint, direction: Direction): Snake? {
    val nextPoint = point.shift(direction)
    val elemAtNextPoint = board[nextPoint]
    if (elemAtNextPoint in listOf(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP)) {
        return Snake(body = listOf(nextPoint, point), evilTickRemind = 0, enemy = false)
    }
    if (elemAtNextPoint == HEAD_EVIL) {
        return Snake(body = listOf(nextPoint, point), evilTickRemind = Prices.EVIL_DURATION /*by default*/, enemy = false)
    }
    if (elemAtNextPoint in listOf(ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP)) {
        return Snake(body = listOf(nextPoint, point), evilTickRemind = 0, enemy = true)
    }
    if (elemAtNextPoint == ENEMY_HEAD_EVIL) {
        return Snake(body = listOf(nextPoint, point), evilTickRemind = Prices.EVIL_DURATION /*by default*/, enemy = true)
    }
    if (elemAtNextPoint !in listOf(BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN, BODY_RIGHT_UP,
                    ENEMY_BODY_HORIZONTAL, ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP)) {
        return null // dead snake
    }
    val nextDirection = when (direction) {
        Direction.UP -> when (elemAtNextPoint) {
            in listOf(BODY_LEFT_DOWN, ENEMY_BODY_LEFT_DOWN) -> Direction.LEFT
            in listOf(BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_DOWN) -> Direction.RIGHT
            in listOf(BODY_VERTICAL, ENEMY_BODY_VERTICAL) -> Direction.UP
            else -> throw IllegalStateException("Not expected $elemAtNextPoint after moving $direction at point $nextPoint")
        }
        Direction.DOWN -> when (elemAtNextPoint) {
            in listOf(BODY_LEFT_UP, ENEMY_BODY_LEFT_UP) -> Direction.LEFT
            in listOf(BODY_RIGHT_UP, ENEMY_BODY_RIGHT_UP) -> Direction.RIGHT
            in listOf(BODY_VERTICAL, ENEMY_BODY_VERTICAL) -> Direction.DOWN
            else -> throw IllegalStateException("Not expected $elemAtNextPoint after moving $direction at point $nextPoint")
        }
        Direction.LEFT -> when (elemAtNextPoint) {
            in listOf(BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_DOWN) -> Direction.DOWN
            in listOf(BODY_RIGHT_UP, ENEMY_BODY_RIGHT_UP) -> Direction.UP
            in listOf(BODY_HORIZONTAL, ENEMY_BODY_HORIZONTAL) -> Direction.LEFT
            else -> throw IllegalStateException("Not expected $elemAtNextPoint after moving $direction at point $nextPoint")
        }
        Direction.RIGHT -> when (elemAtNextPoint) {
            in listOf(BODY_LEFT_DOWN, ENEMY_BODY_LEFT_DOWN) -> Direction.DOWN
            in listOf(BODY_LEFT_UP, ENEMY_BODY_LEFT_UP) -> Direction.UP
            in listOf(BODY_HORIZONTAL, ENEMY_BODY_HORIZONTAL) -> Direction.RIGHT
            else -> throw IllegalStateException("Not expected $elemAtNextPoint after moving $direction at point $nextPoint")
        }
    }
    val nextSnake = parseSnakeFromTail(board, nextPoint, nextDirection) ?: return null
    return Snake(
            body = nextSnake.body + point,
            evilTickRemind = nextSnake.evilTickRemind,
            enemy = nextSnake.enemy
    )
}

