package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.Set;

public class Player {

    private Socket connection;
    private Thread read;
    private Thread send;
    public Player(String ip,int port) {
        try {

            connection = new Socket(ip,port );
            Read();
            Send();

        }catch (ConnectException e)
        {
            try {
                Thread.sleep(2*1000L);
                connection = new Socket(ip,port);
                if(connection==null)
                {
                    System.out.println("Can't connect");
                }

            } catch (InterruptedException | IOException interruptedException) {
                System.err.println("Error while connecting again to socket or while sleeping");
            }
        }
        catch (IOException exception) {
            System.err.println("IO Error -Player constructor");
        }
    }
    public void startPlayer()
    {
        this.read.start();
        this.send.start();
    }
    public void Send()
    {
        Scanner scanner = new Scanner(System.in);
        this.send  = new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream out = null;
                try {
                    out = new DataOutputStream(connection.getOutputStream());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                while (true)
                {
                    String msg = scanner.nextLine();
                    try {
                        out.writeUTF(msg);
                        Thread.sleep(500);
                        if(msg.equals("Exit"))
                        {
                            out.close();
                            break;
                        }
                    } catch (IOException | InterruptedException exception) {
                        System.err.println("Error in IO -Sending Client ");
                    }

                }
            }
        });

    }
    public void Read()
    {

        this.read  = new Thread(new Runnable() {
            @Override
            public void run() {
                DataInputStream in = null;
                try {
                    in = new DataInputStream(connection.getInputStream());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                while (true)
                {
                    try {
                        String msg = in.readUTF();
                        if(msg.equals("Close"))
                        {
                            in.close();
                            connection.close();
                            break;
                        }
                            System.out.println(msg);
                    }
                    catch (IOException exception) {
                        System.err.println("Error in reading - Read Client");
                    }
                }
            }
        });
    }

}
