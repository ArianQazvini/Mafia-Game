package com.company;

import com.company.Logic.Server;

public class ServerMain {
    public static void main(String [] args)
    {
        Server server = new Server(8000);
        server.start();
    }
}
