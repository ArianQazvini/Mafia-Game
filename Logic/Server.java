package com.company.Logic;

import com.company.Civilians.*;
import com.company.Mafias.GodFather;
import com.company.Mafias.Lecter;
import com.company.Mafias.SimpleMafia;
import com.company.PlayerData;
import com.company.TimeCounter;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

public class Server {
    private ArrayList<PlayerData> playersData = new ArrayList<>();
    private ArrayList<UserThread> userThreads = new ArrayList<>();
    private ArrayList<Role> roles = new ArrayList<>();
    private ServerSocket serverSocket = null;
    private String name;
    private int port;
    private int ready;
    public Server(int port)
    {

        this.name = "Server1";
        this.port= port;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException exception) {
            System.err.println("IO Error in Server Constructor");
        }
        CreateRoles();
    }
    private void CreateRoles()
    {
        CityDoctor cityDoctor = new CityDoctor();
        Detective detective = new Detective();
        DieHard dieHard = new DieHard();
        Mayor mayor = new Mayor();
        Professional professional = new Professional();
        Psychologist psychologist = new Psychologist();
        SimpleCivilian simpleCivilian = new SimpleCivilian();
        //----------------------------------------------------
        GodFather godFather = new GodFather();
        Lecter lecter = new Lecter();
        SimpleMafia simpleMafia = new SimpleMafia();
        //----------------------------------
        roles.add(cityDoctor);
        roles.add(detective);
        roles.add(dieHard);
        roles.add(mayor);
        roles.add(professional);
        roles.add(psychologist);
        roles.add(simpleCivilian);
        roles.add(godFather);
        roles.add(lecter);
        roles.add(simpleMafia);
    }
    private Role RandomRoll()
    {
        Random random = new Random();
        if(roles.size()==0)
        {
           int choice = random.nextInt(2);
           if (choice==1)
           {
               roles.add(new SimpleCivilian());
           }
           else
           {
               roles.add(new SimpleMafia());
           }
        }
        else
        {
        }
        int choice = random.nextInt(roles.size());
        Role temp = roles.get(choice);
        roles.remove(choice);
        return temp;
    }
    public void execute()
    {
        int count=0;
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Server is waiting on port: "+port);
            while (count<10)
            {
               Socket socket = serverSocket.accept();
               count++;
               System.out.println("New user connected");
               System.out.println("Number of Players:"+count);
               UserThread temp = new UserThread(socket,this);
               userThreads.add(temp);
               temp.start();
            }
            System.out.println("Main characters are fixed");
            System.out.println("Server is waiting 5 seconds for other clients");
                serverSocket.setSoTimeout(5*1000);
                while (true)
                {
                    try {
                        Socket socket = serverSocket.accept();
                        count++;
                        System.out.println("New user connected");
                        System.out.println("Number of Players:"+count);
                        UserThread temp = new UserThread(socket,this);
                        userThreads.add(temp);
                        temp.start();
                    }catch (SocketTimeoutException e)
                    {
                        System.out.println("Socket timed out");
                        break;
                    }
                }
        }
        catch (IOException exception) {
            System.err.println("Error about IO in serverside");
        }
    }
    public synchronized boolean CheckUsername(String username,PlayerData data)
    {
         if(playersData.size()==0)
         {
             data.setUsername(username);
             data.setRole(RandomRoll());
             playersData.add(data);
             return true;
         }
         else
         {
             for (int i=0;i<playersData.size();i++)
             {
                 if(playersData.get(i).getUsername().equals(username))
                     return false;
             }
             data.setUsername(username);
             data.setRole(RandomRoll());
             playersData.add(data);
             return true;
         }
    }
    public synchronized void SendAll(String string,UserThread ut)
    {
        if(!ut.getData().getRole().isCanChat())
        {
            ut.Receive("You can't send message cause you  are muted");
        }
        else
        {
            ut.Receive("You sent: "+string+"✓✓");
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).equals(ut))
                {
                }
                else
                {
                    if (userThreads.get(i).getData().getRole().isCanChat())
                    {
                        userThreads.get(i).Receive(ut.getData().getUsername()+" sent: "+string);
                    }
                }

            }
        }

    }
    private void SendAll(String msg)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().isCanChat())
            {
                userThreads.get(i).Receive(msg);
            }
        }
    }
    public void Mute(String name,int sleep)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getUsername().equals(name))
            {
                userThreads.get(i).Receive("You are Muted");
                userThreads.get(i).getData().getRole().setCanChat(false);
                userThreads.get(i).setSleep(sleep);
            }
        }
    }
    public void UnMute(String name)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getUsername().equals(name))
            {
                userThreads.get(i).getData().getRole().setCanChat(true);
                userThreads.get(i).setSleep(0);

            }
        }
    }
    public void RemoveThread(UserThread thread)
    {
        Iterator<UserThread> it = userThreads.iterator();
        String name = "";
        while (it.hasNext())
        {
            UserThread temp  = it.next();
            if(temp.equals(thread))
            {
                name = temp.getData().getUsername();
                System.out.println(name+" disconnected");
                it.remove();
                break;
            }
        }
        Iterator<PlayerData> it2 = playersData.iterator();
        while (it2.hasNext())
        {
            PlayerData temp = it2.next();
            if(temp.getUsername().equals(name))
            {
                it2.remove();
                break;
            }
        }
    }
    private void CheckTime(int sleep)
    {
        Timer timer = new Timer();
        timer.schedule(new TimeCounter(),0,1000);
        try {
            Thread.sleep(sleep* 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
    }

}