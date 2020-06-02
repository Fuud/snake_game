package ru.codebattle.client.api;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.codebattle.client.api.BoardElement.APPLE;
import static ru.codebattle.client.api.BoardElement.BODY_HORIZONTAL;
import static ru.codebattle.client.api.BoardElement.BODY_LEFT_DOWN;
import static ru.codebattle.client.api.BoardElement.BODY_LEFT_UP;
import static ru.codebattle.client.api.BoardElement.BODY_RIGHT_DOWN;
import static ru.codebattle.client.api.BoardElement.BODY_RIGHT_UP;
import static ru.codebattle.client.api.BoardElement.BODY_VERTICAL;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_HORIZONTAL;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_LEFT_DOWN;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_LEFT_UP;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_RIGHT_DOWN;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_RIGHT_UP;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_VERTICAL;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_SLEEP;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_DOWN;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_LEFT;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_RIGHT;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_UP;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_INACTIVE;
import static ru.codebattle.client.api.BoardElement.FLYING_PILL;
import static ru.codebattle.client.api.BoardElement.FURY_PILL;
import static ru.codebattle.client.api.BoardElement.GOLD;
import static ru.codebattle.client.api.BoardElement.HEAD_DEAD;
import static ru.codebattle.client.api.BoardElement.HEAD_DOWN;
import static ru.codebattle.client.api.BoardElement.HEAD_EVIL;
import static ru.codebattle.client.api.BoardElement.HEAD_FLY;
import static ru.codebattle.client.api.BoardElement.HEAD_LEFT;
import static ru.codebattle.client.api.BoardElement.HEAD_RIGHT;
import static ru.codebattle.client.api.BoardElement.HEAD_SLEEP;
import static ru.codebattle.client.api.BoardElement.HEAD_UP;
import static ru.codebattle.client.api.BoardElement.START_FLOOR;
import static ru.codebattle.client.api.BoardElement.STONE;
import static ru.codebattle.client.api.BoardElement.TAIL_INACTIVE;
import static ru.codebattle.client.api.BoardElement.WALL;

public class GameBoard {

    @Getter
    public final String prettyBoardString;

    public GameBoard(String boardString) {
        this.boardString = boardString.replace("\n", "");
        this.size = (int) Math.sqrt(boardString.length());
        String prettyBoard = "";
        for (int i = 0; i < size; i++) {
            prettyBoard+=this.boardString.substring(i * size, size * (i + 1))+"\n";
        }
        this.prettyBoardString = prettyBoard;
        this.board = new BoardElement[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                this.board[x][y] = BoardElement.valueOf(boardString.charAt(y * size + x));
            }
        }
    }

    @Getter
    private String boardString;


    @Getter
    private BoardElement[][] board;

    @Getter
    public int size;

    public BoardPoint getMyHead() {
        return findFirstElement(HEAD_DEAD, HEAD_DOWN, HEAD_UP, HEAD_LEFT, HEAD_RIGHT, HEAD_EVIL,
                HEAD_FLY, HEAD_SLEEP);
    }

    public List<BoardPoint> getWalls() {
        return findAllElements(WALL);
    }

    public List<BoardPoint> getStones() {
        return findAllElements(STONE);
    }

    public boolean isBarrierAt(BoardPoint point) {
        return getBarriers().contains(point);
    }

    public List<BoardPoint> getApples() {
        return findAllElements(APPLE);
    }

    public boolean amIEvil() {
        return findAllElements(HEAD_EVIL).contains(getMyHead());
    }

    public boolean amIFlying() {
        return findAllElements(HEAD_FLY).contains(getMyHead());
    }

    public List<BoardPoint> getFlyingPills() {
        return findAllElements(FLYING_PILL);
    }

    public List<BoardPoint> getFuryPills() {
        return findAllElements(FURY_PILL);
    }

    public List<BoardPoint> getGold() {
        return findAllElements(GOLD);
    }

    public List<BoardPoint> getStartPoints() {
        return findAllElements(START_FLOOR);
    }

    private List<BoardPoint> getBarriers() {
        return findAllElements(WALL, START_FLOOR, ENEMY_HEAD_SLEEP, ENEMY_TAIL_INACTIVE, TAIL_INACTIVE, STONE);
    }

    public boolean hasElementAt(BoardPoint point, BoardElement element) {
        if (point.isOutOfBoard(getSize())) {
            return false;
        }

        return getElementAt(point) == element;
    }

    public BoardElement getElementAt(BoardPoint point) {
        return board[point.getX()][point.getY()];
    }

    public void printBoard() {
        System.out.println(prettyBoardString);
    }

    public BoardPoint findElement(BoardElement elementType) {
        for (int i = 0; i < getSize() * getSize(); i++) {
            BoardPoint pt = getPointByShift(i);
            if (hasElementAt(pt, elementType)) {
                return pt;
            }
        }
        return null;
    }

    public BoardPoint findFirstElement(BoardElement... elementType) {
        for (int i = 0; i < getSize() * getSize(); i++) {
            BoardPoint pt = getPointByShift(i);

            for (BoardElement elemType : elementType) {
                if (hasElementAt(pt, elemType)) {
                    return pt;
                }
            }
        }
        return null;
    }

    public List<BoardPoint> findAllElements(BoardElement... elementType) {
        List<BoardPoint> result = new ArrayList<>();

        for (int i = 0; i < getSize() * getSize(); i++) {
            BoardPoint pt = getPointByShift(i);

            for (BoardElement elemType : elementType) {
                if (hasElementAt(pt, elemType)) {
                    result.add(pt);
                }
            }
        }

        return result;
    }

    public boolean hasElementAt(BoardPoint point, BoardElement... elements) {
        return Arrays.stream(elements).anyMatch(element -> hasElementAt(point, element));
    }

    private BoardPoint getPointByShift(int shift) {
        return new BoardPoint(shift % getSize(), shift / getSize());
    }

    public boolean isNotEvilEnemyHead(BoardElement boardElement) {
        return boardElement == BoardElement.ENEMY_HEAD_DOWN
                || boardElement == BoardElement.ENEMY_HEAD_LEFT
                || boardElement == BoardElement.ENEMY_HEAD_RIGHT
                || boardElement == BoardElement.ENEMY_HEAD_UP;
    }

    public boolean isEnemyBody(BoardElement boardElement) {
        return boardElement == ENEMY_BODY_HORIZONTAL
                || boardElement == ENEMY_BODY_LEFT_DOWN
                || boardElement == ENEMY_BODY_LEFT_UP
                || boardElement == ENEMY_BODY_RIGHT_DOWN
                || boardElement == ENEMY_BODY_RIGHT_UP
                || boardElement == ENEMY_BODY_VERTICAL;
    }

    public boolean isEnemyTail(BoardElement boardElement) {
        return boardElement == ENEMY_TAIL_END_DOWN
                || boardElement == ENEMY_TAIL_END_UP
                || boardElement == ENEMY_TAIL_END_LEFT
                || boardElement == ENEMY_TAIL_END_RIGHT;
    }

    public boolean isStone(BoardElement boardElement) {
        return boardElement == STONE;
    }

    public boolean isMyBody(BoardElement boardElement) {
        return boardElement == BODY_HORIZONTAL
                || boardElement == BODY_LEFT_DOWN
                || boardElement == BODY_LEFT_UP
                || boardElement == BODY_VERTICAL
                || boardElement == BODY_RIGHT_DOWN
                || boardElement == BODY_RIGHT_UP;
    }

    public static int distance(BoardPoint myHead, BoardPoint currPoint) {
        return Math.abs(myHead.getX() - currPoint.getX()) + Math.abs(myHead.getY() - currPoint.getY());
    }
}
