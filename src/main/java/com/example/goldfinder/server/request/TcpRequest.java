package com.example.goldfinder.server.request;

import java.net.Socket;

public class TcpRequest extends Request {
    public Socket socket;
   public TcpRequest(String message,Socket socket) {
       super(message);
       this.socket = socket;
   }

   public java.net.InetAddress getAddress(){
       return socket.getInetAddress();
   }

    public int getPort(){
         return socket.getPort();
    }

}
