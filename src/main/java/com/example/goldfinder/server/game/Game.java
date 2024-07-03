package com.example.goldfinder.server.game;

import com.example.goldfinder.BotClient;
import com.example.goldfinder.server.AppServer;
import com.example.goldfinder.server.Grid;
import com.example.goldfinder.server.request.Request;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Game {
    Grid grid;
    public static int ROW_COUNT;
    public static int COLUMN_COUNT;
    public static int MAX_PLAYER;
    public HashMap<String, Player> players = new HashMap<>();
    public HashMap<String, int[]> playerPositions = new HashMap<>();
    public boolean gameStarted = false;
    public boolean gameEnded = false;
    public boolean botIsComming = false;

    public boolean solo;
    public String gamemode;
    public boolean soloPlayerCapture = false;

    public Game(int row, int column, int maxPlayer, boolean solo) {
        ROW_COUNT = row;
        COLUMN_COUNT = column;
        MAX_PLAYER = maxPlayer;
        grid = new Grid(ROW_COUNT, COLUMN_COUNT, new Random());
        this.solo = solo;
    }

    public abstract boolean acceptMessage(Request request) throws IOException;

    public abstract void addPlayer(Player player);

    public abstract boolean isPositionOccupied(int x, int y, String playerRole) throws IOException;

    public abstract void handleMovement(String direction, Request request) throws IOException;

    public boolean isFull() {
        return players.size() >= MAX_PLAYER;
    }

    public abstract void setPositionsAndRoles();
    public boolean acceptPlayer(Request request) {
        if (players.size() < MAX_PLAYER) {
            System.out.println("Player accepted" + request.getAddress() + " " + request.getPort());
            return true;
        } else {
            System.out.println("Max player reached");
            return false;
        }
    }
    public void handleMessage(Request request) throws IOException {
        String message = request.getMessage();
        System.out.println("Message : " + message);
        if (message == null) {
            System.out.println("message is null");
            return;
        }
        if (!acceptMessage(request)) {
            return;
        }
        String[] function = request.getAllFunctions();
        String firstFunction = request.getFirstFunction();
        if (firstFunction.equals("LEADER")) {
            LeaderBoard.loadLeaderBoard();
            AppServer.sendMessage("SCORE " + LeaderBoard.getLeaderBoard(Integer.parseInt(function[1].trim())) + " END", request);
            return;
        }

        if (!gameStarted) {
            if (firstFunction.contains("GAME_JOIN_GoldFinder") || firstFunction.contains("GAME_JOIN_CopsRobbers")) {
                // lance la partie si le joueur rejoint
                Player player = new Player(function[1], request);
                String playerKey = request.getAddress().toString() + ":" + request.getPort();
                players.put(playerKey, player);

                System.out.println(player.getName() + " joined the game" + " " + players.size() + "/" + MAX_PLAYER);

                if (solo && !botIsComming) {
                    // fill the server with bots
                    System.out.println("Solo mode");
                    AtomicInteger botCount = new AtomicInteger(0);
                    botIsComming = true;
                    for (int i = 0; i < MAX_PLAYER - 1; i++) {
                        // thread of the bot
                        new Thread(() -> {
                            try {
                                BotClient bot = new BotClient("localhost",7777, gamemode, "Bot" + botCount.incrementAndGet());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    }

                }



                if (players.size() == MAX_PLAYER) {
                    setPositionsAndRoles();

                    gameStarted = true;
                    System.out.println("Game started");
                    for (Map.Entry<String, Player> entry : players.entrySet()) {
                        AppServer.sendMessage("GAME_START END", entry.getValue().getRequest());
                    }
                }
                return;
            } else {
                System.out.println("Game has not started yet");
                return;
            }
        }

        int[] playerPosition = playerPositions.get(request.getAddress().toString() + ":" + request.getPort());
        switch (function[0].trim()) {
            case "dir":
                handleMovement(function[1], request);
                break;
            case "SURROUNDING":
                System.out.println("SURROUNDING...");
                surrounding(playerPosition[0], playerPosition[1], request);
                break;
            case "POSITION":
                position(request);
                break;
            case "BREAKWALL":
                if (players.get(request.getAddress().toString() + ":" + request.getPort()).breakWall) {
                    grid.breakWalls(playerPosition[0], playerPosition[1]);
                    players.get(request.getAddress().toString() + ":" + request.getPort()).breakWall = false;
                } else {
                    System.out.println("Player does not have break wall");
                    AppServer.sendMessage("INVALIDMOVE END", request);
                }
                break;
            default:
                System.out.println("+Invalid message : " + function[0]);
        }
    }

    public void position(Request request) throws IOException {
        int[] playerPosition = playerPositions.get(request.getAddress().toString() + ":" + request.getPort());
        AppServer.sendMessage("POSITION " + playerPosition[0] + " " + playerPosition[1] + " END", request);
    }

    public void surrounding(int x, int y, Request request) throws IOException {
        String up = "UP:" + ((grid.upWall(x, y)) ? "WALL" : getGridCellItem(x, y - 1));
        String down = "DOWN:" + ((grid.downWall(x, y)) ? "WALL" : getGridCellItem(x, y + 1));
        String left = "LEFT:" + ((grid.leftWall(x, y)) ? "WALL" : getGridCellItem(x - 1, y));
        String right = "RIGHT:" + ((grid.rightWall(x, y)) ? "WALL" : getGridCellItem(x + 1, y));

        String surrounding = up + " " + down + " " + left + " " + right;
        AppServer.sendMessage(surrounding + " END", request);
    }

    public String getGridCellItem(int x, int y) {
        // return player
        for (Map.Entry<String, int[]> entry : playerPositions.entrySet()) {
            if (entry.getValue()[0] == x && entry.getValue()[1] == y) {
                // return player role
                return players.get(entry.getKey()).getRole();
            }
        }
        return grid.getItem(x, y);
    }

    public boolean hasPlayer(InetAddress address, int port) {
        return players.containsKey(address.toString() + ":" + port);
    }


    // check if the player is on an item
    public void checkItemOnCell(int x, int y, Player player) throws IOException {
        String item = grid.getItem(x, y);
        if (!item.equals("EMPTY")) {
            System.out.println("Item found : " + item);
            switch (item) {
                case "GOLD":
                    player.incrementScore();
                    AppServer.sendMessage("GOLD END", player.getRequest());
                    grid.removeItem(x, y);
                    break;
                case "SLOW":
                    grid.removeItem(x, y);
                    AppServer.sendMessage("SLOW END", player.getRequest());
                    break;
                case "TELEPORT":
                    int[] teleportPosition = getTeleportPosition();
                    playerPositions.put(player.getRequest().getAddress().toString() + ":" + player.getRequest().getPort(), teleportPosition);
                    grid.removeItem(x, y);
                    AppServer.sendMessage("POSITION " + teleportPosition[0] + " " + teleportPosition[1] + " END", player.getRequest());
                    break;
                case "BREAKWALL":
                    if(!player.breakWall){
                        System.out.println("Player got break wall");
                        player.breakWall = true;
                        grid.removeBreakWall(x, y);
                    } else {
                        System.out.println("Player already has break wall");
                    }
                    break;
            }
        }

    }
    private int[] getTeleportPosition() {
        // return a position without player
        List<int[]> allPositions = new ArrayList<>();
        for (int i = 0; i < COLUMN_COUNT; i++) {
            for (int j = 0; j < ROW_COUNT; j++) {
                allPositions.add(new int[] { i, j });
            }
        }
        for (Map.Entry<String, int[]> entry : playerPositions.entrySet()) {
            allPositions.remove(entry.getValue());
        }
        Collections.shuffle(allPositions);
        return allPositions.remove(0);
    }

}
