package com.example.goldfinder.server;

import com.example.goldfinder.server.game.CopsRobbers;
import com.example.goldfinder.server.game.Game;
import com.example.goldfinder.server.game.GoldFinder;
import com.example.goldfinder.server.game.LeaderBoard;
import com.example.goldfinder.server.request.Request;
import com.example.goldfinder.server.request.TcpRequest;
import com.example.goldfinder.server.request.UdpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

public class AppServer {
    private static ArrayList<Game> games = new ArrayList<>();
    private static ArrayList<GoldFinder> goldFinders = new ArrayList<>();
    private static ArrayList<CopsRobbers> copsRobbers = new ArrayList<>();

    public static final int ROW_COUNT = 25;
    public static final int COLUMN_COUNT = 25;
    public static int MAX_PLAYER = 10;
    public static int MAX_COPS = 1;
    private static DatagramSocket socket;
    private static ServerSocket tcpServerSocket;
    public static int serverPort = 7777;

    public static void main(String[] args) {
        System.out.println("Game Server is listening on port " + serverPort);
        try {
            new AppServer();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public AppServer() throws SocketException {
        startUdpServer();
        startTcpServer();
    }

    public static Game getGameFromPlayerIP(InetAddress address, int port) {

        for(int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            if (game.hasPlayer(address, port)) {
                if (game.gameEnded){
                    games.remove(game);
                    return null;
                } else {
                    return game;
                }
            }
        }
        return null;
    }



    public void startUdpServer() throws SocketException {
        socket = new DatagramSocket(serverPort);
        new Thread(() -> {
            while (true) {
                try {
                    receiveUdpMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startTcpServer() {
        try {
            tcpServerSocket = new ServerSocket(serverPort);
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = tcpServerSocket.accept();
                        System.out.println("New client connected");
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        handleTcpConnection(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleTcpConnection(Socket clientSocket) {
        new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while (true) {
                    String message = in.readLine();
                    if (message != null) {
                        System.out.println("Received message : " + message);
                        handleMessage(new TcpRequest(message, clientSocket));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void receiveUdpMessage() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Received message : " + message);
        handleMessage(new UdpRequest(message, packet));
    }



    public static void sendMessage(String message, TcpRequest request) throws IOException {
        PrintWriter out = new PrintWriter(request.socket.getOutputStream(), true);
        out.println(message);
        System.out.println("Sent message : " + message);

    }

    public static void sendMessage(String message, UdpRequest request) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket response = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
        socket.send(response);
        System.out.println("Sent message : " + message);
    }

    public static void sendMessage(String message, Request request) throws IOException {
        if (request instanceof TcpRequest) {
            sendMessage(message, (TcpRequest) request);
        } else {
            sendMessage(message, (UdpRequest) request);
        }

    }

    public synchronized static void handleMessage(Request request) throws IOException {
        String message = request.getMessage();
        if (message == null) {
            System.out.println("message is null");
            return;
        }


        Game game = getGameFromPlayerIP(request.getAddress(), request.getPort());
        String firstFunction = request.getFirstFunction();
        System.out.println("Handling message : " + firstFunction);
        if (game == null){
            if (firstFunction.contains("GAME_JOIN")) {
                if (firstFunction.contains("GAME_JOIN_GoldFinder")) {

                    if (!firstFunction.contains("GAME_JOIN_GoldFinder_Offline")){// search for a game that is not full
                        for (GoldFinder goldFinder : goldFinders) {
                            if (!goldFinder.gameStarted) {
                                game = goldFinder;
                                break;
                            }
                        }
                        if (game == null) {
                            game = new GoldFinder(ROW_COUNT, COLUMN_COUNT, MAX_PLAYER, false);
                            goldFinders.add((GoldFinder) game);
                        }
                    }
                    else{ // search for a game that is not full
                        for (GoldFinder goldFinder : goldFinders) {
                            if (!goldFinder.gameStarted && goldFinder.solo) {
                                game = goldFinder;
                                break;
                            }
                        }
                        if (game == null) {
                            game = new GoldFinder(ROW_COUNT, COLUMN_COUNT, MAX_PLAYER, true);
                            goldFinders.add((GoldFinder) game);
                        }
                    }



                } else {
                    if (!firstFunction.contains("GAME_JOIN_CopsRobbers_Offline")){// search for a game that is not full
                        for (CopsRobbers copsRobber : copsRobbers) {
                            if (!copsRobber.gameStarted) {
                                game = copsRobber;
                                break;
                            }
                        }
                        if (game == null) {
                            game = new CopsRobbers(ROW_COUNT, COLUMN_COUNT, MAX_PLAYER, MAX_COPS, false);
                            copsRobbers.add((CopsRobbers) game);
                        }
                    }
                    else{ // search for a game that is not full
                        for (CopsRobbers copsRobber : copsRobbers) {
                            if (!copsRobber.gameStarted && copsRobber.solo) {
                                game = copsRobber;
                                break;
                            }
                        }
                        if (game == null) {
                            game = new CopsRobbers(ROW_COUNT, COLUMN_COUNT, MAX_PLAYER, MAX_COPS, true);
                            copsRobbers.add((CopsRobbers) game);
                        }
                    }
                }
                games.add(game);
                game.handleMessage(request);
            }
        } else {
            game.handleMessage(request);
        }
        if (firstFunction.contains("LEADER")) {
            LeaderBoard.loadLeaderBoard();
            System.out.println("Sending leader board");
            sendMessage("SCORE " + LeaderBoard.getLeaderBoard(Integer.parseInt(request.getAllFunctions()[1].trim())) + " END", request);
        }
    }
}
