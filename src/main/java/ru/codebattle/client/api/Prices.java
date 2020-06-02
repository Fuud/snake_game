package ru.codebattle.client.api;

public class Prices {
    public static final int EVIL_DURATION = 10;

    public enum S {
        NONE_COST, APPLE_COST, GOLD_COST, STONE_COST, ENEMY_SEGMENT_COST,
        OUR_SNAKE_SEGMENT_COST,
        PILL_COST, SUICIDE_COST, KILLED_BY_ENEMY_COST;
    }


    public static int price(S something) {
        return Coster.DESERT.cost(something);
    }

    enum Coster {
        // для текущей карты, где рельеф простой.
        // удлиняемся, если есть возможность берём ярость и производные.
        // золото не так актуально, хочется посмотреть куси.
        DESERT {
            @Override
            int cost(S something) {
                switch (something) {
                    case NONE_COST:
                        return 0;
                    case APPLE_COST:
                        return 6;
                    case GOLD_COST:
                        return 10;
                    case STONE_COST:
                        return 3;
                    case ENEMY_SEGMENT_COST:
                        return 15;
                    case OUR_SNAKE_SEGMENT_COST:
                        return -10;
                    case PILL_COST:
                        return 4;
                    case SUICIDE_COST:
                        return -100000 / 2;
                    case KILLED_BY_ENEMY_COST:
                        return -100000;
                }
                return 0;
            }
        },

        //много злобы камней и яблок
        TRIPLE_TWO {
            @Override
            int cost(S something) {
                switch (something) {
                    case NONE_COST:
                        return 0;
                    case APPLE_COST:
                        return 12;
                    case GOLD_COST:
                        return 20;
                    case STONE_COST:
                        return 6;
                    case ENEMY_SEGMENT_COST:
                        return 60;
                    case OUR_SNAKE_SEGMENT_COST:
                        return -60;
                    case PILL_COST:
                        return 1;
                    case SUICIDE_COST:
                        return -100000 / 2;
                    case KILLED_BY_ENEMY_COST:
                        return -100000;
                }
                return 0;
            }
        };

        abstract int cost(S something);
    }


}
