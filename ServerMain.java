package com.company;

import com.company.Logic.Server;

public class ServerMain {
    public static void main(String [] args)
    {
        Server server = new Server(8080);
        server.execute();
    }
}
