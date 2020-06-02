package ru.codebattle.client.newgame

import ru.codebattle.client.api.BoardElement.FURY_PILL
import ru.codebattle.client.api.BoardElement.STONE
import ru.codebattle.client.api.Prices
import kotlin.math.max

class Game(initialBoard: String) {
    var tick: Int = 0
    var board = Board(initialBoard)
    var snakes = findSnakes(board)
    val ourSnake
        get() = snakes.single { !it.enemy }

    val enemySnakes
        get() = snakes.filter { it.enemy }

    fun step(newBoardStr: String) {
        tick++
        println("TICK $tick")

        val newBoard = Board(newBoardStr)
        val newSnakes = findSnakes(newBoard)
                .map { newSnake ->
                    // find old copy and copy fury and stone count from old one
                    val oldSnake = snakes.find { oldSnake ->
                        newSnake.body.indexOf(oldSnake.body[0]) == 1
                    } ?: throw IllegalStateException("Cannot find old copy of snake $newSnake")

                    return@map newSnake.copy(evilTickRemind = max(0, oldSnake.evilTickRemind - 1), stoneCount = oldSnake.stoneCount) to oldSnake
                }
                .map { (newSnake, oldSnake) ->
                    // check new fury
                    if (board[newSnake.body[0]] == FURY_PILL) {
                        newSnake.copy(evilTickRemind = oldSnake.evilTickRemind + Prices.EVIL_DURATION -1 ) to oldSnake
                    } else {
                        newSnake to oldSnake
                    }
                }
                .map { (newSnake, oldSnake) ->
                    var stoneInc: Int = 0
                    // add new stone
                    if (board[newSnake.body[0]] == STONE) {
                        stoneInc++
                    }
                    //remove acted stone
                    if (newBoard[oldSnake.body.last()] == STONE) {
                        stoneInc--
                    }
                    newSnake.copy(stoneCount = oldSnake.stoneCount + stoneInc) to oldSnake

                }
                .map { (newSnake, _) -> newSnake }

        snakes = newSnakes
        board = newBoard

    }

}