package com.example.goldfinder.controller;

import com.example.goldfinder.Client;
import com.example.goldfinder.UiClient;

import java.io.IOException;

public abstract class Controller {

    protected Client client;
    public static final String DISPATCH_SERVER_IP = "127.0.0.1";
    public static final int DISPATCH_SERVER_PORT = 6666;


    public Controller(Client client) {
        this.client = client;
    }

    public abstract void startConnection() throws IOException;

    public abstract void sendPacket(String message);

    public abstract boolean receivePacket();
    public abstract void launchParty();

    public abstract void closeConnection();


}
