package com.example.goldfinder.controller;

import com.example.goldfinder.Client;
import com.example.goldfinder.UiClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpController extends Controller{
    // TODO : les pieces ne disparraissent pas si un autre joueur les prends
    DatagramChannel channel;
    String serverIp = "127.0.0.1";
    int serverPort = 6666;

    public UdpController(Client client) throws IOException {
        super(client);
    }

    public UdpController(Client client, String serverIp, int serverPort) throws IOException {
        super(client);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }


    public void startConnection() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
    }

    public void sendPacket(String message) {
        System.out.println("Sending message : " + message);
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            channel.send(buffer, new InetSocketAddress(serverIp, serverPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean receivePacket() {
        try {
            if(channel == null || !channel.isOpen()) {
                return false;
            }
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketAddress sender = channel.receive(buffer);
            if (sender != null) {
                // Flip the buffer to prepare it for reading
                buffer.flip();
                String message = new String(buffer.array(), 0, buffer.limit());
                System.out.println("Received message : " + message);
                return client.handleReponse(message);
            }
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
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
