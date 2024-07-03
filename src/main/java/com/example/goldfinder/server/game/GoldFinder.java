package com.example.goldfinder.server.game;

import com.example.goldfinder.server.AppServer;
import com.example.goldfinder.server.request.Request;

import java.io.IOException;
import java.util.*;

public class GoldFinder extends Game {


    //constructor
    public GoldFinder(int row, int column, int maxPlayer, boolean solo) {
        super(row, column, maxPlayer, solo);
        gamemode = "GoldFinder";
    }

    public boolean acceptMessage(Request request) throws IOException {
        // during the game if the sender is not a player then ignore the message
        if (!players.containsKey(request.getAddress().toString() + ":" + request.getPort())) {
            if (request.getFirstFunction().contains("GAME_JOIN_GoldFinder")) {
                return acceptPlayer(request);
            } else if(request.getFirstFunction().equals("LEADER")){
                return true;
            } else {
                System.out.println("Invalid message");
                return false;
            }
        }
        return true;
    }

    public void addPlayer(Player player) {
        String playerKey = player.getRequest().getAddress().toString() + ":" + player.getRequest().getPort();
        players.put(playerKey, player);
    }

    @Override
    public boolean isPositionOccupied(int x, int y, String playerRole) throws IOException {
        for (Map.Entry<String, int[]> entry : playerPositions.entrySet()) {
            if (entry.getValue()[0] == x && entry.getValue()[1] == y) {
                if(players.get(entry.getKey()).getRole().equals(playerRole)){
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isPositionOccupied(int x, int y) {
        for (int[] position : playerPositions.values()) {
            if (position[0] == x && position[1] == y) {
                return true;
            }
        }
        return false;
    }

    public void handleMovement(String direction, Request request) throws IOException {
        int[] playerPosition = playerPositions.get(request.getAddress().toString() + ":" + request.getPort());
        System.out.println(playerPosition[0] + " " + playerPosition[1]);
        switch (direction.trim()) {
            case "UP":

                if (grid.upWall(playerPosition[0], playerPosition[1])
                        || isPositionOccupied(playerPosition[0], playerPosition[1] - 1)) {
                    System.out.println("Invalid move UP");
                    AppServer.sendMessage("INVALIDMOVE END", request);
                    return;
                } else {
                    playerPosition[1]--;
                    System.out.println("Valid move UP");
                    AppServer.sendMessage("VALIDMOVE END", request);
                }
                break;
            case "DOWN":
                if (grid.downWall(playerPosition[0], playerPosition[1])
                        || isPositionOccupied(playerPosition[0], playerPosition[1] + 1)) {
                    System.out.println("Invalid move DOWN");
                    AppServer.sendMessage("INVALIDMOVE END", request);
                    return;
                } else {
                    playerPosition[1]++;
                    System.out.println("Valid move DOWN");
                    AppServer.sendMessage("VALIDMOVE END", request);
                }
                break;
            case "LEFT":
                if (grid.leftWall(playerPosition[0], playerPosition[1])
                        || isPositionOccupied(playerPosition[0] - 1, playerPosition[1])) {
                    System.out.println("Invalid move LEFT");
                    AppServer.sendMessage("INVALIDMOVE END", request);
                    return;
                } else {
                    playerPosition[0]--;
                    System.out.println("Valid move LEFT");
                    AppServer.sendMessage("VALIDMOVE END", request);
                }
                break;
            case "RIGHT":
                if (grid.rightWall(playerPosition[0], playerPosition[1])
                        || isPositionOccupied(playerPosition[0] + 1, playerPosition[1])) {
                    System.out.println("Invalid move RIGHT");
                    AppServer.sendMessage("INVALIDMOVE END", request);
                    return;
                } else {
                    playerPosition[0]++;
                    System.out.println("Valid move RIGHT");
                    AppServer.sendMessage("VALIDMOVE END", request);
                }
                break;

            default:
                System.out.println("Invalid move" + direction);
                AppServer.sendMessage("INVALIDMOVE END", request);
                return;
        }

        checkItemOnCell(playerPosition[0], playerPosition[1], players.get(request.getAddress().toString() + ":" + request.getPort()));

        if (grid.goldCount == 0) {
            System.out.println("Game over");
            gameStarted = false;

            StringBuilder scores = new StringBuilder();
            for (Map.Entry<String, Player> entry : players.entrySet()) {
                scores.append(entry.getValue().getName()).append(":").append(entry.getValue().getScore())
                        .append(" ");
                LeaderBoard.addScore(entry.getValue().getName(), entry.getValue().getScore());
            }
            LeaderBoard.saveLeaderBoard();

            for (Map.Entry<String, Player> entry : players.entrySet()) {
                AppServer.sendMessage("GAME_END " + scores + " END", entry.getValue().getRequest());
            }
            gameEnded = true;
        }

        // if the player moved to a new position send a surrounding message to all
        // players
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            int[] currentPlayerPosition = playerPositions.get(entry.getKey());
            if (currentPlayerPosition != null) {
                position(entry.getValue().getRequest());
                surrounding(currentPlayerPosition[0], currentPlayerPosition[1], entry.getValue().getRequest());
            }
        }
    }

    @Override
    public void setPositionsAndRoles() {
        // give for each player a random different position
        List<int[]> allPositions = new ArrayList<>();
        for (int i = 0; i < COLUMN_COUNT; i++) {
            for (int j = 0; j < ROW_COUNT; j++) {
                allPositions.add(new int[] { i, j });
            }
        }

        Collections.shuffle(allPositions);

        for (Map.Entry<String, Player> entry : players.entrySet()) {
            playerPositions.put(entry.getKey(), allPositions.remove(0));
            entry.getValue().setRole("FINDER");
        }
    }


}