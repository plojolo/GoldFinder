package com.example.goldfinder.server.request;

import javafx.scene.chart.PieChart;

import java.net.DatagramPacket;

public abstract class Request {
    String request;
    public Request(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public boolean isEnding() {
        return request.endsWith("END");
    }

    public String getMessage() {
        if (isEnding()) {
            return request.substring(0, request.length() - 3);
        }
        return null;
    }

    public String getFirstFunction() {
        return getMessage().split(":")[0];
    }

    public String[] getAllFunctions() {
        return getMessage().split(":");
    }

    public abstract java.net.InetAddress getAddress();

    public abstract int getPort();




}
