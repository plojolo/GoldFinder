package com.example.goldfinder;

import com.example.goldfinder.controller.Controller;
import com.example.goldfinder.controller.TcpController;
import com.example.goldfinder.controller.UdpController;
import com.example.goldfinder.server.AppServer;
import com.example.goldfinder.server.DispatchServer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static com.example.goldfinder.server.game.GoldFinder.COLUMN_COUNT;
import static com.example.goldfinder.server.game.GoldFinder.ROW_COUNT;


public class BotClient extends Client{

    public Controller controller;
    public boolean isTCP = false;

    String gameChoice = "GoldFinder";
    String gameMode;

    boolean sendMove = false;

    String name;

    int slow = 0;

    public static void main(String[] args) throws IOException {
        BotClient botClient = new BotClient();
        try {
            botClient.initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // goto the dispatch server and join the game
    public BotClient() throws IOException {
        super();
        this.gameMode = "Online";
        initialize();
    }

    // join the game with the given server ip and port
    public BotClient(String serverIp, int serverPort,String gameChoice,String name) throws IOException {
        controller = new UdpController(this, serverIp, serverPort);
        controller.startConnection();
        this.gameChoice = gameChoice;
        this.gameMode = "Offline";
        this.name = name;
        joinButton();
    }

    public void initialize() throws IOException {
        controller = new UdpController(this);
        controller.startConnection();

        joinButton();
        handleMove();
    }

    private void onConnectionTypeChanged(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        // if game is started, do not allow to change connection type

            try {
            switch (newValue) {
                case "UDP":
                    controller = new UdpController(this);
                    isTCP = false;
                    break;
                case "TCP":
                    controller = new TcpController(this);
                    isTCP = true;
                    break;
                default:
            }
            } catch (IOException e) {
                throw new RuntimeException(e);


        }
    }



    public String getUsername() {
        return name;
    }
    public String getGameChoice() {

        return gameChoice;
    }
    
    public void joinButton() throws IOException {
        if (!gameStarted && !(getUsername() == null)) {
            controller.startConnection();
            // check if fx application thread
            if(Platform.isFxApplicationThread()) {

            } else {
            }
            controller.sendPacket("GAME_JOIN_"+getGameChoice()+gameMode()+":" + getUsername() + " END");
            gameStarted = true;
            controller.launchParty();
        }
    }

    public String gameMode() {
        return "_" + gameMode;
    }

    public void reset() throws IOException {
        gameStarted = false;
        initialize();
    }

    public boolean handleReponse(String response) throws IOException {
        // check if the message end with END
        if (!response.endsWith("END")) {
            return false;
        } else {
            response.replace("END", "");
        }

        String[] messageParts = response.split(" ");
        String function = messageParts[0].split(":")[0];

        switch (function) {
            case "GAME_START":
                System.out.println("Game started");
                sendMove = true;
                handleMove();
                break;
            case "GAME_END":
                System.out.println("Game ended");
                System.exit(0);
                break;
            case "POSITION":
                break;
            case "UP":
                break;
            case "VALIDMOVE":
                return true;
            case "INVALIDMOVE":
                return false;
            case "SCORE":
                System.out.println("Score : ");
                System.out.println(Arrays.toString(response.split("\n")));
                break;
            case "GOLD":
                System.out.println("You found gold");
                break;
            case "CAPTURE":
                System.out.println("You were captured");
                reset();
                break;
            case "SLOW":
                slow = 10;
                break;
            case "REDIRECT":
                String serverIp = messageParts[0].split(":")[1];
                int serverPort = Integer.parseInt(messageParts[0].split(":")[2]);
                System.out.println("Redirecting to : " + serverIp + ":" + serverPort);
                controller.closeConnection();
                if(isTCP) {
                    controller = new TcpController(this, serverIp, serverPort);
                } else {
                    controller = new UdpController(this, serverIp, serverPort);
                }
                gameStarted = false;
                joinButton();
                break;
            default:
                System.out.println("Invalid response : " + response);
                return false;
        }
        return false;
    }

    @Override
    public void handleMove(KeyEvent keyEvent) {

    }

    public void handleMove() {
        while (sendMove) {
            if (slow > 0) {
                // wait 0.5 seconds
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                slow--;
            }
            String direction = getDirection();
            controller.sendPacket("dir:" + direction + " END");
            // wait 0.5 seconds
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public String getDirection() {
        String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};
        return directions[(int) (Math.random() * 4)];
    }


}
