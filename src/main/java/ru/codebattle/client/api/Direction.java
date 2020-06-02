package ru.codebattle.client.api;

public enum Direction {
    LEFT {
        @Override
        public BoardPoint move(BoardPoint boardPoint) {
            return boardPoint.shiftLeft();
        }

        @Override
        public Direction opposite() {
            return RIGHT;
        }
    }, RIGHT {
        @Override
        public BoardPoint move(BoardPoint boardPoint) {
            return boardPoint.shiftRight();
        }

        @Override
        public Direction opposite() {
            return LEFT;
        }
    }, UP {
        @Override
        public BoardPoint move(BoardPoint boardPoint) {
            return boardPoint.shiftTop();
        }

        @Override
        public Direction opposite() {
            return DOWN;
        }
    }, DOWN {
        @Override
        public BoardPoint move(BoardPoint boardPoint) {
            return boardPoint.shiftBottom();
        }

        @Override
        public Direction opposite() {
            return UP;
        }
    };

    public abstract BoardPoint move(BoardPoint boardPoint);
    public abstract Direction opposite();
}
