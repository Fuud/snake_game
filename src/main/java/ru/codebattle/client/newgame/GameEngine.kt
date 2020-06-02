package ru.codebattle.client.newgame

import ru.codebattle.client.api.BoardElement.*
import ru.codebattle.client.api.Direction
import ru.codebattle.client.api.GameBoard
import ru.codebattle.client.api.GameBoard.distance
import ru.codebattle.client.api.SnakeAction
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalTime
class GameEngine {

    private var game: Game? = null

    fun step(gameBoard: GameBoard): SnakeAction {
        // обслужим спец. случаи
        val headElem = gameBoard.myHead?.let { gameBoard.getElementAt(it) }
        if (headElem in listOf(null, HEAD_DEAD, HEAD_SLEEP)) {
            println("HEAD: ${headElem?.name ?: "HEADLESS"}")
            game = null
            return SnakeAction(Direction.RIGHT)
        }

        if (game == null) {
            game = Game(gameBoard.prettyBoardString)
        } else {
            try {
                game!!.step(gameBoard.prettyBoardString)
            } catch (e: Exception) {
                e.printStackTrace()
                game = Game(gameBoard.prettyBoardString)
            }
        }

        val (result, duration) = measureTimedValue {
            chooseSnakeAction(game!!)
        }

        println("Time: ${duration.inMilliseconds}ms")

        return result
    }

    private fun chooseSnakeAction(game: Game): SnakeAction {
        val badDirections = game.findDefinitelyBadDirections()
        val action = game.findPath(game.ourSnake, excludeAtFirstMove = badDirections, doNotStepBack = false)
        println("badDirections = ${badDirections}, action#1=${action}")
        return createAction(game, action);
    }

    private fun createAction(game: Game, action: SnakeAction?): SnakeAction {
        if (shouldSuicide(game, action?.direction)) {
            return SnakeAction.suicide()
        }

        requireNotNull(action)

        return SnakeAction(action.direction, action.isAct || game.shouldPreAct())
    }

    private fun shouldSuicide(game: Game, dir: Direction?): Boolean {
        if (dir == null) {
            println("suicide:  dir = ${dir}")
            return true;

        }
        val element = game.board[dir.move(game.ourSnake.head)]
        if (element == null || element == WALL) {
            println("suicide: element = ${element} dir = ${dir}")
            return true
        }

        return false;
    }

    private fun Game.findDefinitelyBadDirections(): Set<Direction> {
        val ourHead = ourSnake.head

        val sameLineBadDirection = enemySnakes
                .filter { enemy ->
                    distance(enemy.head, ourHead) in 2..3 &&
                            (enemy.head.x == ourHead.x || enemy.head.y == ourHead.y) &&
                            distance(enemy.neck, ourHead) > 1
                }
                .filter { enemy ->
                    weAreScared(enemy, distance(enemy.head, ourHead) - 1)
                }
                .map { enemy ->
                    when {
                        enemy.head.x > ourHead.x -> Direction.RIGHT
                        enemy.head.x < ourHead.x -> Direction.LEFT
                        enemy.head.y < ourHead.y -> Direction.UP
                        else -> Direction.DOWN
                    }
                }

        val diagonalDirection = enemySnakes
                .filter { enemy ->
                    distance(enemy.head, ourHead) == 2 &&
                            (enemy.head.x != ourHead.x && enemy.head.y != ourHead.y) &&
                            distance(enemy.neck, ourHead) > 1
                }
                .filter { enemy ->
                    weAreScared(enemy, 2)
                }
                .flatMap { enemy ->
                    val byX = when {
                        enemy.head.x > ourHead.x -> Direction.RIGHT
                        else -> Direction.LEFT
                    }
                    val byY = when {
                        enemy.head.y < ourHead.y -> Direction.UP
                        else -> Direction.DOWN
                    }
                    listOf(byX, byY)
                }

        val mimoProhodilDirections = enemySnakes
                /*
                 step1:
                 ->..
                 ...^
                 ...|

                 step2
                 -->^
                 ...|
                 */
                .filter { enemy ->
                    distance(enemy.head, ourHead) == 3 &&
                            (enemy.head.x != ourHead.x && enemy.head.y != ourHead.y) &&
                            distance(enemy.neck, ourHead) > 2
                }
                .filter { enemy ->
                    weAreScared(enemy, 2)
                }
                .flatMap { enemy ->
                    val byX = when {
                        enemy.head.x > ourHead.x -> Direction.RIGHT
                        else -> Direction.LEFT
                    }
                    val byY = when {
                        enemy.head.y < ourHead.y -> Direction.UP
                        else -> Direction.DOWN
                    }
                    listOf(byX, byY)
                }

        return (sameLineBadDirection + diagonalDirection + mimoProhodilDirections).toSet()
    }

    private fun Game.weAreScared(enemy: Snake, moves: Int): Boolean {
        val weWillBeEvilAtNextMove = ourSnake.evilTickRemind > moves
        val enemyWillBeEvil = enemy.evilTickRemind > moves
        val weAreLonger = ourSnake.body.size - enemy.body.size >= 2
        val enemyIsLonger = enemy.body.size - ourSnake.body.size >= 2
        val itIsLastEnemy = enemySnakes.size == 1

        return if ((weWillBeEvilAtNextMove && enemyWillBeEvil) || (!weWillBeEvilAtNextMove && !enemyWillBeEvil)) {
            when {
                weAreLonger -> false
                enemyIsLonger -> true
                else -> !itIsLastEnemy // should we end game now?
            }
        } else {
            !weWillBeEvilAtNextMove
        }
    }

    private fun Game.shouldPreAct(): Boolean {
        val ourSnake = ourSnake
        if (ourSnake.stoneCount > 0) {
            val tail = ourSnake.tail
            return Direction.values()
                    .map { d -> d.move(tail) }
                    .any { p -> board.get(p) in listOf(ENEMY_HEAD_DOWN, ENEMY_HEAD_UP, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_EVIL) }
        }
        return false
    }
}