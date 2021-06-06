package com.company.Logic;

import com.company.Civilians.CityDoctor;
import com.company.Civilians.Civilian;
import com.company.Civilians.DieHard;
import com.company.Mafias.Lecter;
import com.company.Mafias.Mafia;
import com.company.PlayerData;

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
    private String  choosenPlayer = null;
    private String MafiaVote = null;
    private String Vote = null;
    private String poll= null;
    private String MayorDecision=null;
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
//            while (!data.getRole().isCanChat())
//            {
//                Thread.sleep(500);
//                if(data.getRole().isCanChat())
//                    break;
//            }
            while (true)
            {

                String message = in.readUTF();
                Thread.sleep(200);
                if(VotingMode)
                {
                    boolean validity = false;
                    while (!validity)
                    {
                        if(this.server.GetPlayer(message)==null)
                        {
                            out.writeUTF("Player is not in list");
                        }
                        else if (this.server.GetPlayer(message).getData().getUsername().equals(this.getData().getUsername()))
                        {
                            out.writeUTF("Can't vote to your self");
                        }
                        else
                        {
                            out.writeUTF("Done");
                            poll = message;
                            VotingMode = false;
                            validity = true;
                        }
                        if(validity)
                        {
                        }
                        else
                        {
                            message = in.readUTF();
                        }
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
                                ChoosePlayer();
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
                                ChoosePlayer();
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
                        ChoosePlayer();
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
                                ChoosePlayer();
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
                            this.poll="Yes";
                            out.writeUTF("Choose the player you want to kill");
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
                                    ChoosePlayer();
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
                                    ChoosePlayer();
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
                        else
                        {
                            this.poll="NO";
                            ChoosePlayerMode=false;
                        }

                    }
                    else if(this.getData().getRole().getCharacter().equals(Position.DIEHARD))
                    {
                        if(message.equals("YES"))
                        {
                            this.poll = "YES";
                            this.server.setDiehardPermission(true);
                            ChoosePlayerMode = false;
                        }
                        else
                        {
                            this.poll="NO";
                            ChoosePlayerMode=false;
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
                    if(message.equals("Exit"))
                    {
                        out.writeUTF("Close");
                        socket.close();
                        server.RemoveThread(this,"Normal");
                        break;
                    }
                    this.server.SendAll(message,this);
                }
            }
        }
        catch (SocketException e)
        {
            this.server.RemoveThread(this,"UnNormal");
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
    public String ChoosePlayer()
    {
            return choosenPlayer;
    }
    public String MafiaVote()
    {
        return this.MafiaVote;
    }
    public String poll()
    {
        return poll;
    }
    public String MayorDecision()
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
