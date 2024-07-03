package com.example.goldfinder;

import com.example.goldfinder.controller.TcpController;
import com.example.goldfinder.controller.UdpController;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.Arrays;

public abstract class Client {

    public boolean gameStarted = false;
    public abstract void initialize() throws IOException;
    public abstract String getUsername();
    public abstract String getGameChoice();

    public abstract void joinButton() throws IOException;

    public abstract void reset() throws IOException;

    public abstract boolean handleReponse(String response) throws IOException;



    public abstract void handleMove(KeyEvent keyEvent);
}
