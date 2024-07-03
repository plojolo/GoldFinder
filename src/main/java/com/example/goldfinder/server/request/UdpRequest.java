package com.example.goldfinder.server.request;

import java.net.DatagramPacket;

public class UdpRequest extends Request {
    DatagramPacket packet;

    public UdpRequest(String message,DatagramPacket packet) {
        super(message);
        this.packet = packet;
    }

    public java.net.InetAddress getAddress(){
        return packet.getAddress();
    }

    public int getPort(){
        return packet.getPort();
    }
}
