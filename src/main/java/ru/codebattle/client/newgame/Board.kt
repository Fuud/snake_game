package ru.codebattle.client.newgame

import ru.codebattle.client.api.BoardElement
import ru.codebattle.client.api.BoardPoint
import kotlin.math.pow

class Board(boardString: String) {
    private val clearString = boardString.replace("\n", "").trim()
    val size: Int = clearString.length.toDouble().pow(0.5).toInt()
    private val board: List<List<BoardElement>> = clearString.toList().chunked(size).map { it -> it.map { BoardElement.valueOf(it) } }

    fun <T> mapEachElement(action: (point: BoardPoint, elem: BoardElement) -> T): List<T> {
        val result = mutableListOf<T>()
        board.forEachIndexed { y, line ->
            line.forEachIndexed { x, elem ->
                result.add(action(BoardPoint(x, y), elem))
            }
        }
        return result
    }

    operator fun get(point: BoardPoint): BoardElement? {
        if (point.isOutOfBoard(size)){
            return null
        }else {
            return board[point.y][point.x]
        }
    }
}