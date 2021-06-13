package com.company.Logic;

import com.company.Civilians.*;
import com.company.Mafias.Lecter;
import com.company.Mafias.Mafia;
import com.company.PlayerData;

import javax.swing.plaf.RootPaneUI;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class UserThread extends Thread{
    private PlayerData data;
    private Socket socket;
    private Server server;
    private DataOutputStream out =null;
    private DataInputStream in = null;
    private int sleep=0;
    private boolean isRegistered = false;
    private boolean canStartGame= false;
    private boolean ChoosePlayerMode = false;
    private boolean VotingMode = false;
    private boolean MafiaVotingMode = false;
    private boolean MayorMode  = false;
    private boolean DeadMode = false;
    private String  choosenPlayer = null;
    private String MafiaVote = null;
    private String Vote = null;
    private String poll= null;
    private String MayorDecision=null;
    private String Watch = null;
    public  String RESET = "\u001B[0m";
    public  String BLACK = "\u001B[30m";
    public  String RED = "\u001B[31m";
    public  String GREEN = "\u001B[32m";
    public  String YELLOW = "\u001B[33m";
    public  String BLUE = "\u001B[34m";
    public  String PURPLE = "\u001B[35m";
    public  String CYAN = "\u001B[36m";
    public  String WHITE = "\u001B[37m";
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
                if(data.getRole() instanceof Mafia)
                {
                    out.writeUTF("Your role is "+RED+data.getRole().getCharacter()+RESET);
                }
                else
                {
                    out.writeUTF("Your role is "+GREEN+data.getRole().getCharacter()+RESET);
                }
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
//            while (!data.getRole().isCanChat())
//            {
//                Thread.sleep(500);
//                if(data.getRole().isCanChat())
//                    break;
//            }
            while (true)
            {

                String message = in.readUTF();
                Thread.sleep(100);
                if(message.equals("Exit"))
                {
//                    out.writeUTF("Close");
//                    socket.close();
//                    in.close();
//                    out.close();
//                    server.RemoveThread(this,"Normal");
                    break;
                }
                if(DeadMode)
                {
                    if(message.equals("YES"))
                    {
                        this.Watch = "YES";
                        DeadMode=false;
                        this.server.WatchRequestCompleted();
                    }
                    else
                    {
                        this.Watch="NO";
                        this.server.WatchRequestCompleted();
                        break;
//                        out.writeUTF("Close");
//                        socket.close();
//                        in.close();
//                        out.close();
//                        server.RemoveThread(this,"Normal");
                    }
                }
                else if(VotingMode)
                {
                 //   boolean validity = false;
                  //  while (!validity)
                 //   {
                        if(this.server.GetPlayer(message)==null)
                        {
                            out.writeUTF("Player is not in list");
                        }
                        else if (this.server.GetPlayer(message).getData().getUsername().equals(this.getData().getUsername()))
                        {
                            out.writeUTF("Can't vote to your self");
                        }
                        else {
                            if (poll == null) {
                                out.writeUTF("Done");
                            } else {
                                out.writeUTF("Your vote changed from " + poll + " to " + message);
                            }
                            poll = message;
                            //   VotingMode = false;
                            //      validity = true;
                            //  }
//                        if(validity)
//                        {
//                        }
//                        else
//                        {
//                            Thread.sleep(500);
//                            if(!VotingMode)
//                            {
//                                validity=true;
//                            }
//                            else
//                            {
//                                message = in.readUTF();
//                            }
//                        }
                    }
                }
                else if(MafiaVotingMode)
                {
                    boolean validity = false;
                    while (!validity)
                    {
                        if (this.server.GetPlayer(message)==null)
                        {
                            out.writeUTF("Player is not in list");
                        }
                        else if (this.server.GetPlayer(message).getData().getRole() instanceof Mafia)
                        {
                            out.writeUTF("Choose from civilians");
                        }
                        else
                        {
                            MafiaVote = message;
                            MafiaVotingMode = false;
                            validity = true;
                        }
                        if(validity)
                        {
                        }
                        else {
                            message=in.readUTF();
                        }
                    }

                }
                else if(ChoosePlayerMode)
                {
                    if(this.getData().getRole().getCharacter().equals(Position.GODFATHER))
                    {
                        boolean validity = false;
                        while (!validity)
                        {
                            if(this.server.GetPlayer(message)==null)
                            {
                                out.writeUTF("Player is not in list");
                            }
                            else if(this.server.GetPlayer(message).getData().getRole() instanceof Mafia)
                            {
                                out.writeUTF("Choose from civilians");
                            }
                            else
                            {
                                choosenPlayer=message;
                                ChoosePlayerMode = false;
                                validity=true;
                            }
                            if(validity)
                            {
                            }
                            else {
                                message=in.readUTF();
                            }
                        }
                    }
                    else if (this.getData().getRole().getCharacter().equals(Position.LECTER))
                    {
                        boolean validity = false;
                        while (!validity)
                        {
                            if(this.server.GetPlayer(message)==null)
                            {
                                out.writeUTF("Player is not in list");
                            }
                            else if(this.server.GetPlayer(message).getData().getRole() instanceof Civilian)
                            {
                                out.writeUTF("Choose from mafias");
                            }
                            else
                            {
                                if(this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.LECTER))
                                {
                                    Lecter temp = (Lecter) this.data.getRole();
                                    if(temp.getSelfHeal()==0)
                                    {
                                        temp.SelfHeal();
                                    }
                                    else
                                    {
                                        out.writeUTF("You have healed yourself once before-Choose someone else");
                                        message = in.readUTF();
                                        while (this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.LECTER))
                                        {
                                            out.writeUTF("You have healed yourself once before-Choose someone else");
                                            message = in.readUTF();
                                        }
                                    }
                                }
                                choosenPlayer=message;
                                ChoosePlayerMode = false;
                                validity=true;
                            }
                            if(validity)
                            {
                            }
                            else {
                                message=in.readUTF();
                            }
                        }
                    }
                    else if (this.getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
                    {
                        while (this.server.GetPlayer(message)==null)
                        {
                            out.writeUTF("Not valid player");
                            message = in.readUTF();
                        }
                        if(this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
                        {
                            CityDoctor temp = (CityDoctor) this.data.getRole();
                            if(temp.getSelfHeal()==0)
                            {
                                temp.SelfHeal();
                            }
                            else
                            {
                                out.writeUTF("You have healed yourself once before-Choose someone else");
                                message = in.readUTF();
                                while (this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
                                {
                                    out.writeUTF("You have healed yourself once before-Choose someone else");
                                    message = in.readUTF();
                                }
                            }
                        }
                        choosenPlayer=message;
                        ChoosePlayerMode = false;
                    }
                    else if(this.getData().getRole().getCharacter().equals(Position.DETECTIVE))
                    {
                        boolean validity =false;
                        while (!validity)
                        {
                            if(this.server.GetPlayer(message)==null)
                            {
                                out.writeUTF("Player not in list");
                            }
                            else if (this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.DETECTIVE))
                            {
                                out.writeUTF("You choosed yourself");
                            }
                            else
                            {
                                choosenPlayer=message;
                                ChoosePlayerMode = false;
                                validity=true;
                            }
                            if(validity)
                            {
                            }
                            else {
                                message=in.readUTF();
                            }
                        }
                    }
                    else if(this.getData().getRole().getCharacter().equals(Position.PROFESSIONAL))
                    {
                        if(message.equals("YES"))
                        {
                            this.poll="YES";
                            out.writeUTF("Choose the player you want to kill");
                            out.writeUTF(this.server.GetAllplayers());
                            message= in.readUTF();
                            boolean validity = false;
                            while (!validity)
                            {
                                if(this.server.GetPlayer(message)==null)
                                {
                                    out.writeUTF("Player is not in list");
                                }
                                else if(this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.PROFESSIONAL))
                                {
                                    out.writeUTF("You choosed yourself!");
                                }
                                else {
                                    choosenPlayer=message;
                                    Professional temp =(Professional) this.getData().getRole();
                                    temp.action(this.server.GetPlayer(message));
                                    validity=true;
                                    out.writeUTF("Done");
                                    ChoosePlayerMode = false;
                                }
                                if(validity)
                                {
                                }
                                else {
                                    message=in.readUTF();
                                }
                            }
                        }
                        else
                        {
                            this.poll="NO";
                            ChoosePlayerMode=false;
                        }
                    }
                    else if(this.getData().getRole().getCharacter().equals(Position.PSYCHOLOGIST))
                    {
                        if(message.equals("YES"))
                        {
                            this.poll = "YES";
                            out.writeUTF("Choose the player you want to mute");
                            out.writeUTF(this.server.GetAllplayers());
                            message= in.readUTF();
                            boolean validity = false;
                            while (!validity)
                            {
                                if(this.server.GetPlayer(message)==null)
                                {
                                    out.writeUTF("Player is not in list");
                                }
                                else if(this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.PSYCHOLOGIST))
                                {
                                    out.writeUTF("You choosed yourself!");
                                }
                                else {
                                    choosenPlayer=message;
                                    Psychologist temp = (Psychologist) this.getData().getRole();
                                    temp.action(server.GetPlayer(message));
                                    validity=true;
                                    out.writeUTF("Done");
                                    ChoosePlayerMode = false;
                                }
                                if(validity)
                                {
                                }
                                else {
                                    message=in.readUTF();
                                }
                            }
                        }
                        else
                        {
                            this.poll="NO";
                            ChoosePlayerMode=false;
                        }

                    }
                    else if(this.getData().getRole().getCharacter().equals(Position.DIEHARD))
                    {
                        DieHard temp = (DieHard) this.getData().getRole();
                        if(temp.getAnounceCount()<2)
                        {
                            if(message.equals("YES"))
                            {
                                this.server.setDiehardPermission(true);
                                temp.AnounceRequest();
                                this.server.setAnouncement(true);
                                out.writeUTF("Done");
                                this.server.setDieHardMode(false);
                                ChoosePlayerMode = false;
                            }
                            else
                            {
                                this.poll="NO";
                                out.writeUTF("Done");
                                ChoosePlayerMode = false;
                            }
                        }
                        else
                        {
                            out.writeUTF("You can't use your ability");
                            ChoosePlayerMode = false;
                        }
                    }
                    if(message.equals("Exit"))
                    {
                        out.writeUTF("Close");
                        socket.close();
                        server.RemoveThread(this,"Normal");
                        break;
                    }
                }
                else if (MayorMode)
                {
                    if(message.equals("YES"))
                    {
                       this.MayorDecision = message;
                       out.writeUTF("Done");
                    }
                    else
                    {
                        this.MayorDecision="NO";
                        out.writeUTF("Done");
                        MayorMode = false;
                    }
                }
                else
                {
                    if(this.server.isPublicChatMode() && message.equals("History"))
                    {
                        this.Receive(this.server.LoadAll());
                    }
//                    if(message.equals("Exit"))
//                    {
//                        out.writeUTF("Close");
//                        socket.close();
//                        server.RemoveThread(this,"Normal");
//                        break;
//                    }
                    if(!message.equals("History"))
                    {
                        this.server.SendAll(message,this);
                    }
                }
            }
            out.writeUTF("Close");
            socket.close();
            in.close();
            out.close();
            server.RemoveThread(this,"Normal");
        }
        catch (SocketException e)
        {
            try
            {
                out.writeUTF("Close");
                socket.close();
                in.close();
                out.close();
                this.server.RemoveThread(this,"UnNormal");
            }catch (IOException ex)
            {
                System.err.println("IOError - in socket Excp UT");
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
    public synchronized void Receive(String str)
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
    public synchronized String ChoosePlayer()
    {
            return choosenPlayer;
    }
    public synchronized String MafiaVote()
    {
        return this.MafiaVote;
    }
    public synchronized String poll()
    {
        return poll;
    }
    public String Watch()
    {
        return Watch;
    }
    public synchronized String MayorDecision()
    {
        return this.MayorDecision;
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
    public void setChoosePlayerMode(boolean choosePlayerMode) {
        ChoosePlayerMode = choosePlayerMode;
    }
    public boolean isChoosePlayerMode() {
        return ChoosePlayerMode;
    }

    public void setVotingMode(boolean votingMode) {
        VotingMode = votingMode;
    }
    public boolean isVotingMode() {
        return VotingMode;
    }
    public void setMafiaVotingMode(boolean mafiaVotingMode) {
        MafiaVotingMode = mafiaVotingMode;
    }
    public boolean isMafiaVotingMode() {
        return MafiaVotingMode;
    }

    public void setMayorMode(boolean mayorMode) {
        MayorMode = mayorMode;
    }
    public void setDeadMode(boolean deadMode) {
        DeadMode = deadMode;
    }
    public void Disconnect()
    {
        try {
            this.out.writeUTF("Close");
            socket.close();
            server.RemoveThread(this,"Normal");
        } catch (IOException exception) {
            System.err.println("Error while disconnecting in userthread");
        }
    }
    public boolean isMayorMode() {
        return MayorMode;
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
