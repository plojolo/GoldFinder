package com.example.goldfinder.server.game;

import com.example.goldfinder.server.request.Request;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Player {
    private Request request;
    private final InetAddress address;
    private final int port;
    private final String name;
    private int score;

    private String role;
    private DatagramPacket packet;
    boolean breakWall = false;

    public Player(String name, Request request) {
        this.request = request;
        this.address = request.getAddress();
        this.port = request.getPort();
        this.name = name;
        this.score = 0;
    }


    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public Request getRequest() {
        return request;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void incrementScore() {
        this.score++;
    }

}