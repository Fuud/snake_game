package ru.codebattle.client;

import ru.codebattle.client.api.GameBoard;
import ru.codebattle.client.api.SnakeAction;
import ru.codebattle.client.newgame.GameEngine;

import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        String host = "http://codebattle-pro-2020s1.westeurope.cloudapp.azure.com/codenjoy-contest/board/player/4y7j4lo74kmhc429dnz1?code=4860080322186741329&gameName=snakebattle";
        SnakeBattleClient client = new SnakeBattleClient(host);
        connect(client);

        Thread.currentThread().join();

        client.initiateExit();
    }

    private static void connect(SnakeBattleClient client) {
        client.run(Main::safeAction);
    }

    private static SnakeAction safeAction(GameBoard theBoard) {
        try {
            SnakeAction action = action(theBoard);
            return action;
        } catch (Exception e) {
            e.printStackTrace();
            return new SnakeAction(false, null);
        }
    }

    static GameEngine gameEngine = new GameEngine();

    private static SnakeAction action(GameBoard theBoard) {
        return gameEngine.step(theBoard);
    }

}
