package com.company.Logic;

import com.company.PlayerData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UserThread extends Thread{
    private PlayerData data;
    private Socket socket;
    private Server server;
    private DataOutputStream out =null;
    private DataInputStream in = null;
    private int sleep=0;
    private boolean isRegistered = false;
    private boolean canStartGame= false;
    public UserThread(Socket socket,Server server)
    {
        this.socket = socket;
        this.server=server;
        this.data = new PlayerData();
    }
    @Override
    public void run()
    {
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            if(data.getUsername()==null)
            {
                  out.writeUTF("Enter your username:");
                  String username = in.readUTF();
                  while (!this.server.CheckUsername(username, this.data))
                  {
                      out.writeUTF("Username is NOT VALID-Try again");
                      username = in.readUTF();
                  }
                  out.writeUTF("Username registered");
//                  Thread.sleep(500);
                  out.writeUTF("Your role is "+data.getRole().getCharacter());
                  this.server.Register(this);
                  out.writeUTF("---------------------");
                  Thread.sleep(500);
                  if(!server.isJoinigFinished())
                  {
                      out.writeUTF("Other players joining or registering is not finished yet!");
                  }
            }
            while (!canStartGame)
            {
                Thread.sleep(1000);
                if(canStartGame)
                    break;
            }
            while (!data.getRole().isCanChat())
            {
                Thread.sleep(500);
                if(data.getRole().isCanChat())
                    break;
            }
            while (data.getRole().isCanChat())
            {
                String message = in.readUTF();
                if(message.equals("Exit"))
                {
                    out.writeUTF("Close");
                    socket.close();
                    server.RemoveThread(this);
                    break;
                }
                this.server.SendAll(message,this);
            }
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println("Something went wrong about IO-UserThread");
        }
        finally {
            try {
                if(in!=null)
                {
                    in.close();
                }
                if(out!=null)
                {
                    out.close();
                }
            }
            catch (IOException e)
            {
                System.err.println("Something went wrong about IO");
            }

        }
    }
    public void Receive(String str)
    {
        try {
            this.out.writeUTF(str);
        } catch (IOException exception) {
            System.err.println("Error in Receive");
        }
    }
    public void AskReady()
    {
        try{
            this.out.writeUTF("Are you ready? YES-NO");
            String answer = this.in.readUTF();
            while (!answer.equals("YES"))
            {
                this.out.writeUTF("Are you ready? YES-NO");
                answer = in.readUTF();
            }
            this.server.Ready();
            canStartGame = true;
            Thread.sleep(400);
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("Error in AskReady method");
        }

    }
    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getSleep() {
        return sleep;
    }
    public void setData(PlayerData data) {
        this.data = data;
    }
    public PlayerData getData() {
        return data;
    }
    public boolean isRegistered() {
        return isRegistered;
    }
    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }
    @Override
    public boolean equals(Object o)
    {
        if (o==this)
            return true;
        else if (!(o instanceof UserThread))
            return false;
        else
        {
            UserThread temp = (UserThread) o;
            if(temp.server.equals(this.server) && temp.getData().getUsername().equals(this.getData().getUsername()))
                return true;
            else
                return false;
        }
    }
}
