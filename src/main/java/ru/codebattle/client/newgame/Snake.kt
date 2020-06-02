package ru.codebattle.client.newgame

import ru.codebattle.client.api.BoardPoint
import ru.codebattle.client.api.Direction

data class Snake(val body: List<BoardPoint>, val evilTickRemind: Int, val stoneCount: Int = 0, val enemy: Boolean) {

    val head = body.first()
    val tail = body.last()
    val neck = body[1]

    val lastMove: Direction = this.run {
        val head = body[0]
        val neck = body[1]
        return@run when {
            head.x > neck.x -> Direction.RIGHT
            head.x < neck.x -> Direction.LEFT
            head.y > neck.y -> Direction.DOWN
            else -> Direction.UP
        }
    }

    fun move(newPoint: BoardPoint, reduceTailBy: Int = 0, evilTickDiff: Int = 0, stoneDiff: Int = 0): Snake {
        val newBody = mutableListOf(newPoint)
        newBody.addAll(body.subList(0, body.size - 1 - reduceTailBy))

        return Snake(
                newBody,
                evilTickRemind = Math.max(0, evilTickRemind - 1 + evilTickDiff),
                stoneCount = stoneCount + stoneDiff,
                enemy = enemy
        )
    }
}