package com.example.goldfinder.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.io.PrintWriter;
import java.util.*;

public class DispatchServer {
    private static DatagramSocket socket;
    private static ServerSocket tcpServerSocket;
    private static final int DISPATCH_SERVER_PORT = 6666;
    private static final int MAX_LOAD = 100;
    private List<GameServerInfo> gameServers;


    public DispatchServer() {
        System.out.println("Initializing dispatch server..");
        this.start();
        this.gameServers = new ArrayList<>();
        gameServers.add(new GameServerInfo("127.0.0.1", 7777, 0));
        gameServers.add(new GameServerInfo("192.168.2.1", 7777, 0));
    }

    public static void main(String[] args) {
        new DispatchServer();
    }

    public void start() {
        startUdpServer();
        startTcpServer();
        System.out.println("Dispatch server started on port " + DISPATCH_SERVER_PORT);
    }

    public void startUdpServer() {
        try {
            socket = new DatagramSocket(DISPATCH_SERVER_PORT);
            new Thread(() -> {
                while (true) {
                    try {
                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        GameServerInfo selectedServer = selectServer();
                        System.out.println("[DISPATCH] New UDP client connected sent to " + selectedServer.getAddress() + ":" + selectedServer.getPort());
                        byte[] redirectBuffer = new byte[1024];
                        // keep the buffer size to 1024 to avoid buffer overflow
                        // and add redirect info
                        redirectBuffer = ("REDIRECT:" + selectedServer.getAddress() + ":" + selectedServer.getPort() + " END").getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(redirectBuffer, redirectBuffer.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);
                        selectedServer.addLoad();
                        System.out.println("[DISPATCH] Load of server " + selectedServer.getAddress() + ":" + selectedServer.getPort() + " is now " + selectedServer.getCurrentLoad());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startTcpServer() {
        try {
            tcpServerSocket = new ServerSocket(DISPATCH_SERVER_PORT);
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = tcpServerSocket.accept();
                        GameServerInfo selectedServer = selectServer();

                        System.out.println("[DISPATCH] New client connected sent to " + selectedServer.getAddress() + ":" + selectedServer.getPort());
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        out.println("REDIRECT:"+selectedServer.getAddress() + ":" + selectedServer.getPort() + " END");
                        selectedServer.addLoad();
                        System.out.println("[DISPATCH] Load of server " + selectedServer.getAddress() + ":" + selectedServer.getPort() + " is now " + selectedServer.getCurrentLoad());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GameServerInfo selectServer() {
        // select most loaded server under MAX_LOAD
        GameServerInfo selectedServer = gameServers.get(0);
        for (GameServerInfo server : gameServers) {
            if (server.getCurrentLoad() > selectedServer.getCurrentLoad() && server.getCurrentLoad() < MAX_LOAD) {
                selectedServer = server;
            }
        }
        return selectedServer;
    }

    static class GameServerInfo {
        private String address;
        private int port;
        private int currentLoad;

        public GameServerInfo(String address, int port, int currentLoad) {
            this.address = address;
            this.port = port;
            this.currentLoad = currentLoad;
        }
        public String getAddress() {
            return address;
        }
        public int getPort() {
            return port;
        }

        public int getCurrentLoad() {
            return currentLoad;
        }

        public void addLoad() {
            currentLoad++;
        }

    }
}
