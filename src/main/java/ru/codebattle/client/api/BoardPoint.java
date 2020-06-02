package ru.codebattle.client.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoardPoint {
    private int x;
    private int y;

    public BoardPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Checks is current point on board or out of range.
     *
     * @param boardSize Board size to compare
     */
    public boolean isOutOfBoard(int boardSize) {
        return x >= boardSize || y >= boardSize || x < 0 || y < 0;
    }

    /**
     * Returns new BoardPoint object shifted left to "delta" points
     */
    public BoardPoint shiftLeft(int delta) {
        return new BoardPoint(x - delta, y);
    }

    /**
     * Returns new BoardPoint object shifted left to 1 point
     */
    public BoardPoint shiftLeft() {
        return shiftLeft(1);
    }

    /**
     * Returns new BoardPoint object shifted right to "delta" points
     */
    public BoardPoint shiftRight(int delta) {
        return new BoardPoint(x + delta, y);
    }

    /**
     * Returns new BoardPoint object shifted right to 1 point
     */
    public BoardPoint shiftRight() {
        return shiftRight(1);
    }

    /**
     * Returns new BoardPoint object shifted top "delta" points
     */
    public BoardPoint shiftTop(int delta) {
        return new BoardPoint(x, y - delta);
    }

    /**
     * Returns new BoardPoint object shifted top 1 point
     */
    public BoardPoint shiftTop() {
        return shiftTop(1);
    }

    /**
     * Returns new BoardPoint object shifted bottom "delta" points
     */
    public BoardPoint shiftBottom(int delta) {
        return new BoardPoint(x, y + delta);
    }

    /**
     * Returns new BoardPoint object shifted bottom 1 point
     */
    public BoardPoint shiftBottom() {
        return shiftBottom(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardPoint that = (BoardPoint) o;

        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    public boolean notEquals(Object o) {
        return !equals(o);
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", x, y);
    }

    @NotNull
    public BoardPoint shift(@NotNull Direction direction) {
        return direction.move(this);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Direction directionTo(BoardPoint other){
        if (this.x != other.x && this.y != other.y){
            throw new IllegalArgumentException("Points should be at the same line. this "+this+", other="+other);
        }
        if (this.x > other.x){
            return Direction.LEFT;
        }else if (this.x < other.x){
            return Direction.RIGHT;
        }else if (this.y > other.y){
            return Direction.UP;
        }else {
            return Direction.DOWN;
        }
    }
}
