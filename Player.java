package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Player class.
 * which gets messages from player and send it to server
 * and also recieve messages from server
 */
public class Player {

    private Socket connection; // client's socket
    private Thread read;
    private Thread send;
    private boolean connected= true;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    /**
     * Instantiates a new Player.
     *
     * @param ip   the ip
     * @param port the port
     */
    public Player(String ip,int port) {
        try {
            connection = new Socket(ip,port );
        }
        catch (ConnectException e)
        {
            System.err.println("Server refused to connect");
        }
        catch (IOException exception) {
            System.err.println("IO Error -Player constructor");
        }
    }

    /**
     * Start player.
     */
    public void startPlayer()
    {
        if(connection!= null)
        {
            Read();
            Send();
        }
    }

    /**
     * Send messages.
     */
    public void Send()
    {
        Scanner scanner = new Scanner(System.in);
        this.send  = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out = new DataOutputStream(connection.getOutputStream());
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
                while (connected)
                {
//                    if(!connected)
//                    {
//                        try {
//                            connection.close();
//                        } catch (IOException exception) {
//                            System.err.println("Error in IO -Sending Client ");
//                        }
//                        break;
//                    }
                    if(!connected)
                    {
                        break;
                    }
                    String msg = scanner.nextLine();
                    try {
                        out.writeUTF(msg);
                        Thread.sleep(900);
                    }
                    catch (IOException | InterruptedException exception) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
//                        System.err.println("Error in IO -Sending Client ");
                    }
                }
            }
        });
        this.send.start();
    }

    /**
     * Read messages from server.
     */
    public void Read()
    {
        this.read  = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in = new DataInputStream(connection.getInputStream());
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
                while (connected)
                {
                    try {
                        String msg = in.readUTF();
                        if(msg.equals("Close"))
                        {
                            Thread.sleep(500);
                            connection.close();
                            setConnected(false);
                            break;
                        }
                            System.out.println(msg);
                    }
                    catch (IOException | InterruptedException exception) {
                        System.err.println("Error in reading - Read Client");
                    }
                }
                try {
                    in.close();
                    out.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
        this.read.start();
    }

    /**
     * Sets connected.
     *
     * @param connected the connected
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Is connected boolean.
     *
     * @return the boolean
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Gets in stream.
     *
     * @return the in
     */
    public DataInputStream getIn() {
        return in;
    }

    /**
     * Gets out stream.
     *
     * @return the out
     */
    public DataOutputStream getOut() {
        return out;
    }
}
