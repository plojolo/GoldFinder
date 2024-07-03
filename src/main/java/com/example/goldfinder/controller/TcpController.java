package com.example.goldfinder.controller;

import com.example.goldfinder.Client;
import com.example.goldfinder.UiClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.*;

public class TcpController extends Controller {
    // TODO : les pieces ne disparraissent pas si un autre joueur les prends
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverIp = "127.0.0.1";
    private int serverPort = 6666;

    public TcpController(Client client) throws IOException {
        super(client);
    }

    public TcpController(Client client, String serverIp, int serverPort) throws IOException {
        super(client);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void startConnection() throws IOException {
        clientSocket = new Socket(serverIp, serverPort);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendPacket(String message) {
        System.out.println("Sending message : " + message);
        out.println(message);

    }

    public boolean receivePacket() {
        try {
            if(clientSocket == null || clientSocket.isClosed()) {
                return false;
            }
            String message = in.readLine();
            System.out.println("Received message : " + message);
            return client.handleReponse(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void launchParty() {
        new Thread(() -> {
            while (client.gameStarted) {
                receivePacket();
            }
        }).start();
    }

    public void closeConnection() {
//        try {
//            in.close();
//            out.close();
//            clientSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
