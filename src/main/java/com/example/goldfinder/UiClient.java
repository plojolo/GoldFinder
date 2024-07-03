package com.example.goldfinder;

import com.example.goldfinder.controller.Controller;
import com.example.goldfinder.controller.TcpController;
import com.example.goldfinder.controller.UdpController;
import com.example.goldfinder.server.AppServer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static com.example.goldfinder.server.game.GoldFinder.COLUMN_COUNT;
import static com.example.goldfinder.server.game.GoldFinder.ROW_COUNT;



public class UiClient extends Client{
    @FXML
    Canvas gridCanvas;
    @FXML
    Label score;
    @FXML
    ListView<String> scoreboard;
    @FXML
    Label infoLabel;
    @FXML
    TextField username;
    @FXML
    ChoiceBox<String> connectionType;
    @FXML
    ChoiceBox<String> gameChoice;
    @FXML
    CheckBox OnlineButton;

    GridView gridView;
    int column, row;
    int[] position = new int[2];
    int[] finalPosition = new int[2];
    private SimpleIntegerProperty currentScore = new SimpleIntegerProperty(0);

    public Controller controller;
    public boolean isTCP = false;

    int slow = 0;

    public void initialize() throws IOException {
        this.gridView = new GridView(gridCanvas, AppServer.COLUMN_COUNT, AppServer.ROW_COUNT);
        gridView.repaint();
        column = AppServer.COLUMN_COUNT;
        row = AppServer.ROW_COUNT;

        score.textProperty().bind(currentScore.asString());

        gridView.paintToken(position[0], position[1]);

        connectionType.valueProperty().addListener(this::onConnectionTypeChanged);

        controller = new UdpController(this);
        controller.startConnection();
    }

    private void onConnectionTypeChanged(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        // if game is started, do not allow to change connection type
        if (gameStarted) {
            connectionType.setValue(oldValue);
            infoLabel.setText("Cannot change connection type while game is running");
        } else {
            try {
                switch (newValue) {
                    case "UDP":
                        controller = new UdpController(this);
                        isTCP = false;
                        System.out.println("UDP connection selected");
                        break;
                    case "TCP":
                        controller = new TcpController(this);
                        isTCP = true;
                        System.out.println("TCP connection selected");
                        break;
                    default:
                        System.out.println("Invalid connection type : " + newValue);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }



    public String getUsername() {
        if (username.getText().isEmpty()) {
            infoLabel.setText("Please enter a username :");
            return null;
        }
        return username.getText();
    }
    public String getGameChoice() {
        if (Objects.equals(gameChoice.getValue(), "GoldFinder")) {
            return "GoldFinder";
        } else {
            return "CopsRobbers";
        }
    }

    public void joinButton() throws IOException {
        if (!gameStarted && !(getUsername() == null)) {
            controller.startConnection();
            System.out.println("Joining game...");
            // check if fx application thread
            if(Platform.isFxApplicationThread()) {
                infoLabel.setText("Waiting for other player to join...");
                currentScore.set(0);
            } else {
                Platform.runLater(() -> infoLabel.setText("Waiting for other player to join..."));
                Platform.runLater(() -> currentScore.set(0));
            }
            controller.sendPacket("GAME_JOIN_"+getGameChoice()+gameMode()+":" + getUsername() + " END");
            gameStarted = true;
            controller.launchParty();
            controller.sendPacket("LEADER:5 END");
            controller.receivePacket();
        }
    }

    public String gameMode() {
        if (OnlineButton.isSelected()) {
            return "_Online";
        } else {
            return "_Offline";
        }
    }

    public void reset() throws IOException {
        gridView = new GridView(gridCanvas, COLUMN_COUNT, ROW_COUNT);
        gridView.repaint();
        column = COLUMN_COUNT;
        row = ROW_COUNT;
        position = new int[2];
        finalPosition = new int[2];
        gridView.paintToken(position[0], position[1]);
        gameStarted = false;
        initialize();
    }

    public boolean handleReponse(String response) throws IOException {
        // check if the message end with END
        if (!response.endsWith("END")) {
            System.out.println("Invalid message format END missing : " + response);
            return false;
        } else {
            response.replace("END", "");
        }

        String[] messageParts = response.split(" ");
        String function = messageParts[0].split(":")[0];

        switch (function) {
            case "GAME_START":
                System.out.println("Game started");
                Platform.runLater(() -> infoLabel.setText("Game started"));
                controller.sendPacket("POSITION END");
                controller.sendPacket("SURROUNDING END");
                break;
            case "GAME_END":
                gridView.repaint();
                gridView.paintToken(finalPosition[0], finalPosition[1]);
                System.out.println("Game ended");
                Platform.runLater(() -> infoLabel.setText("Game ended"));
                Platform.runLater(() -> currentScore.set(currentScore.get() + 1));
                controller.sendPacket("LEADER:5 END");
                controller.receivePacket();
                reset();
                break;
            case "POSITION":
                position[0] = Integer.parseInt(messageParts[1]);
                position[1] = Integer.parseInt(messageParts[2]);
                gridView.paintToken(position[0], position[1]);
                break;
            case "UP":
                handleSurrounding(messageParts);
                break;
            case "VALIDMOVE":
                System.out.println("Valid move");
                Platform.runLater(() -> infoLabel.setText("Valid move"));
                return true;
            case "INVALIDMOVE":
                Platform.runLater(() -> infoLabel.setText("Invalid move"));
                infoLabel.setText("Invalid move");
                return false;
            case "SCORE":
                System.out.println("Score : ");
                System.out.println(Arrays.toString(response.split("\n")));
                response = response.replace("SCORE ", "");
                response = response.replace("END", "");
                response = response.replace(":", " : ");
                response = response.replace("\\n", "\n");
                scoreboard.getItems().clear();
                scoreboard.getItems().addAll(Arrays.asList(response.split("\n")));
                break;
            case "GOLD":
                Platform.runLater(() -> currentScore.set(currentScore.get() + 1));
                System.out.println(currentScore.get());
                break;
            case "CAPTURE":
                System.out.println("You were captured");
                Platform.runLater(() -> infoLabel.setText("You were captured"));
                reset();
                break;
            case "SLOW":
                slow = 10;
                break;
            case "REDIRECT":
                String serverIp = messageParts[0].split(":")[1];
                int serverPort = Integer.parseInt(messageParts[0].split(":")[2]);
                System.out.println("Redirecting to : " + serverIp + ":" + serverPort);
                Platform.runLater(() -> infoLabel.setText("Redirecting to : " + serverIp + ":" + serverPort));
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
                Platform.runLater(() -> infoLabel.setText("Invalid response"));
                return false;
        }
        return false;
    }

    public void handleSurrounding(String[] messageParts) {
        // TODO : hadle others items
        try {gridView.removeHWall(position[0], position[1]);
            if (messageParts[0].contains("GOLD")) {
                gridView.paintGold(position[0], position[1] - 1);
            }  else if (messageParts[0].contains("SLOW")) {
                gridView.paintSlow(position[0], position[1] - 1);
            } else if (messageParts[0].contains("TELEPORT")) {
                gridView.paintTeleport(position[0], position[1] - 1);
            } else if (messageParts[0].contains("BREAKWALL")) {
                gridView.paintBreakWall(position[0], position[1] - 1);
            } else if (messageParts[0].contains("WALL")) {
                gridView.paintHWall(position[0], position[1]);
            }else {
                System.out.println("Removing wall at " + position[0] + "," + (position[1] - 1));
                gridView.removeHWall(position[0], position[1]);
                gridView.removeAllItems(position[0], position[1] - 1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No wall or gold at position " + position[0] + "," + (position[1] - 1));
        }

        try {gridView.removeHWall(position[0], position[1] + 1);
            if (messageParts[1].contains("GOLD")) {
                gridView.paintGold(position[0], position[1] + 1);
            }  else if (messageParts[1].contains("SLOW")) {
                gridView.paintSlow(position[0], position[1] + 1);
            } else if (messageParts[1].contains("TELEPORT")) {
                gridView.paintTeleport(position[0], position[1] + 1);
            } else if (messageParts[1].contains("BREAKWALL")) {
                gridView.paintBreakWall(position[0], position[1] + 1);
            } else if (messageParts[1].contains("WALL")) {
                gridView.paintHWall(position[0], position[1] + 1);
            }else {
                System.out.println("Removing wall at " + position[0] + "," + (position[1] + 1));
                gridView.removeHWall(position[0], position[1] + 1);
                gridView.removeAllItems(position[0], position[1] + 1);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No wall or gold at position " + position[0] + "," + (position[1] + 1));
        }

        try {gridView.removeVWall(position[0], position[1]);
            if (messageParts[2].contains("GOLD")) {
                gridView.paintGold(position[0] - 1, position[1]);
            } else if (messageParts[2].contains("SLOW")) {
                gridView.paintSlow(position[0] - 1, position[1]);
            } else if (messageParts[2].contains("TELEPORT")) {
                gridView.paintTeleport(position[0] - 1, position[1]);
            } else if (messageParts[2].contains("BREAKWALL")) {
                gridView.paintBreakWall(position[0] - 1, position[1]);
            }else if (messageParts[2].contains("WALL")) {
                gridView.paintVWall(position[0], position[1]);
            }  else {
                System.out.println("Removing wall at " + (position[0] - 1) + "," + position[1]);
                gridView.removeVWall(position[0], position[1]);
                gridView.removeAllItems(position[0] - 1, position[1]);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No wall or gold at position " + (position[0] - 1) + "," + position[1]);
        }

        try {gridView.removeVWall(position[0] + 1, position[1]);
            if (messageParts[3].contains("GOLD")) {
                gridView.paintGold(position[0] + 1, position[1]);
            }  else if (messageParts[3].contains("SLOW")) {
                gridView.paintSlow(position[0] + 1, position[1]);
            } else if (messageParts[3].contains("TELEPORT")) {
                gridView.paintTeleport(position[0] + 1, position[1]);
            } else if (messageParts[3].contains("BREAKWALL")) {
                gridView.paintBreakWall(position[0] + 1, position[1]);
            } else if (messageParts[3].contains("WALL")) {
                gridView.paintVWall(position[0] + 1, position[1]);
            }else {
                System.out.println("Removing wall at " + position[0] + 1 + "," + position[1]);
                gridView.removeVWall(position[0] + 1, position[1]);
                gridView.removeAllItems(position[0] + 1, position[1]);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No wall or gold at position " + (position[0] + 1) + "," + position[1]);
        }

        gridView.repaint();
        if (messageParts[0].contains("ROBBER")) {
            gridView.paintRobber(position[0], position[1] - 1);
        } else if (messageParts[0].contains("COP")) {
            gridView.paintCop(position[0], position[1] - 1);
        } else if (messageParts[0].contains("FINDER")) {
            gridView.paintEnemy(position[0], position[1] - 1);
        }


        if (messageParts[1].contains("ROBBER")) {
            gridView.paintEnemy(position[0], position[1] + 1);
        } else if (messageParts[1].contains("COP")) {
            gridView.paintCop(position[0], position[1] + 1);
        } else if (messageParts[1].contains("FINDER")) {
            gridView.paintEnemy(position[0], position[1] + 1);
        }
        if (messageParts[2].contains("ROBBER")) {
            gridView.paintRobber(position[0] - 1, position[1]);
        } else if (messageParts[2].contains("COP")) {
            gridView.paintCop(position[0] - 1, position[1]);
        } else if (messageParts[2].contains("FINDER")) {
            gridView.paintEnemy(position[0] - 1, position[1]);
        }
        if (messageParts[3].contains("ROBBER")) {
            gridView.paintRobber(position[0] + 1, position[1]);
        } else if (messageParts[3].contains("COP")) {
            gridView.paintCop(position[0] + 1, position[1]);
        } else if (messageParts[3].contains("FINDER")) {
            gridView.paintEnemy(position[0] + 1, position[1]);
        }
        gridView.paintToken(position[0], position[1]);
    }


    public void handleMove(KeyEvent keyEvent) {
        if (gameStarted) {
            if (slow > 0) {
                // wait 0.5 seconds
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                slow--;
                return;
            }
            switch (keyEvent.getCode()) {
                case Z:
                    if (position[1] - 1 < 0) {
                        System.out.println("Invalid move UP");
                        return;
                    }
                    controller.sendPacket("dir:UP END");
                    finalPosition[0] = position[0];
                    finalPosition[1] = position[1] - 1;
                    break;
                case S:
                    if (position[1] + 1 >= row) {
                        System.out.println("Invalid move DOWN");
                        return;
                    }
                    controller.sendPacket("dir:DOWN END");
                    finalPosition[0] = position[0];
                    finalPosition[1] = position[1] + 1;
                    break;
                case Q:
                    if (position[0] - 1 < 0) {
                        System.out.println("Invalid move LEFT");
                        return;
                    }
                    controller.sendPacket("dir:LEFT END");
                    finalPosition[0] = position[0] - 1;
                    finalPosition[1] = position[1];
                    break;
                case D:
                    if (position[0] + 1 >= column) {
                        System.out.println("Invalid move RIGHT");
                        return;
                    }
                    controller.sendPacket("dir:RIGHT END");
                    finalPosition[0] = position[0] + 1;
                    finalPosition[1] = position[1];
                    break;
                case B:
                    controller.sendPacket("BREAKWALL END");
                    break;
                default:
                    System.out.println("Invalid key pressed : " + keyEvent.getCode());
            }

            if (gridView.goldAt[position[0]][position[1]]) {
                gridView.removeGold(position[0], position[1]);
            }
            if (gridView.slow[position[0]][position[1]]) {
                gridView.removeSlow(position[0], position[1]);
            }
            if (gridView.teleport[position[0]][position[1]]) {
                gridView.removeTeleport(position[0], position[1]);
            }
            if (gridView.breakWall[position[0]][position[1]]) {
                gridView.removeBreakWall(position[0], position[1]);
            }
            controller.sendPacket("POSITION END");
            controller.sendPacket("SURROUNDING END");
            gridView.repaint();
            gridView.paintToken(position[0], position[1]);

        }
    }
}
