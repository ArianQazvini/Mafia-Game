package com.company.Logic;
import com.company.Civilians.*;
import com.company.Mafias.GodFather;
import com.company.Mafias.Lecter;
import com.company.Mafias.Mafia;
import com.company.Mafias.SimpleMafia;
import com.company.PlayerData;
import com.company.TimeCounter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private ArrayList<PlayerData> playersData = new ArrayList<>();
    private ArrayList<UserThread> userThreads = new ArrayList<>();
    private ArrayList<UserThread> Dead = new ArrayList<>();
    private ArrayList<Role> roles = new ArrayList<>();
    private ServerSocket serverSocket = null;
    private String name;
    private int port;
    private int ready=0;
    private boolean joinigFinished=false;
    private UserThread PsychologistChoice=null;
    private boolean anouncement=false;
    private boolean DiehardPermission= false;
    public Server(int port)
    {

        this.name = "Server1";
        this.port= port;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException exception) {
            System.err.println("IO Error in Server Constructor");
        }

    }
    private void CreateRoles(int number)
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
        if(number==10)
        {}
        else if (number>10)
        {
            for (int i=0;i<(number/3 -3);i++)
            {
                roles.add(new SimpleMafia());
            }
            for (int j=0;j<((number-number/3)-7);j++)
            {
                roles.add(new SimpleCivilian());
            }
        }
    }
    private Role RandomRoll()
    {
        Random random = new Random();
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
            System.out.println("Enter number of players:");
            int max = scanner.nextInt();
            CreateRoles(max);
            System.out.println("Waiting for "+max+" players to join");
            while (count<max)
            {
               Socket socket = serverSocket.accept();
               count++;
               System.out.println("New user connected");
               System.out.println("Number of Players:"+count);
               UserThread temp = new UserThread(socket,this);
               userThreads.add(temp);
               temp.start();
            }
            serverSocket.close();
            System.out.println("Everybody has joined");
                while (!EveryBodyRegistered())
                {
                  Thread.sleep(500);
                }
                SendAll(GetAllplayers()+"All players");
                AskReady();
                while (!CanStartGame())
                {
                    System.out.println("Server is waiting for player to get ready..");
                    Thread.sleep(1000);
                    if(CanStartGame())
                        break;
                }
                SendAll("---------------------------");
                SendAll("Game is going to start");
                SendAll("---------------------------");
                SendAll("***Introduction night***");
                MuteAll();
                MafiaIntroduce();
                Thread.sleep(300);
                MayortoDrIntroduce();
                SendAll("Mafia is going to wake up...");
                Thread.sleep(300);
                UnMuteMafia();
                SendAll("You got only 30 seconds for chatting");
                CheckTime(30);
                MuteAll();
                GodFather();
                DrLecter();
                CityDoctor();
                Detective();
                Professional();
                Psychologist();
                DieHard();
                System.out.println("Check time");
        }
        catch (IOException | InterruptedException exception) {
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
    public void Register(UserThread ut)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).equals(ut))
            {
                userThreads.get(i).setRegistered(true);
                ut.setRegistered(true);
                break;
            }
        }
    }
    private boolean EveryBodyRegistered()
    {
        int count = 0;
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).isRegistered())
                count++;
        }
        if(count==userThreads.size())
        {
            setJoinigFinished(true);
            return true;
        }
        else
            return false;
    }
    private String AllPlayers()
    {
        StringBuilder stringBuilder = new StringBuilder();
        int j=1;
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().isAlive())
            {
                stringBuilder.append((j)).append("-").append(userThreads.get(i).getData().getUsername()).append("\n");
                j++;
            }
        }
        return stringBuilder.toString();
    }
    public String GetAllplayers()
    {
        if(joinigFinished)
            return AllPlayers();
        else
            return null;
    }
    public synchronized void Ready()
    {
        ready++;
    }
    private void ThreadsAskready(int num)
    {
        Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    userThreads.get(num).AskReady();
                }
            });
        thread.start();
    }
    private void AskReady()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            ThreadsAskready(i);
        }
    }
    public boolean CanStartGame()
    {
        if(ready==userThreads.size())
            return true;
        else
            return false;
    }
    public void SendAll(String string,UserThread ut)
    {
        if(!ut.getData().getRole().isCanChat())
        {
            ut.Receive("You can't send messages cause you are muted or sleeping");
        }
        else
        {
            ut.Receive("You : "+string+"✓✓");
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).equals(ut))
                {
                }
                else
                {
                    if (userThreads.get(i).getData().getRole().isCanChat())
                    {
                        userThreads.get(i).Receive(ut.getData().getUsername()+" : "+string);
                    }
                }

            }
        }

    }
    private void MafiaIntroduce()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole() instanceof Mafia)
            {
                stringBuilder.append(userThreads.get(i).getData().getUsername()).append(" is ").append(userThreads.get(i).getData().getRole().getCharacter()).append("\n");
            }
        }
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole() instanceof Mafia)
            {
                userThreads.get(i).Receive(stringBuilder.toString());
            }
        }
    }
    private void MayortoDrIntroduce()
    {
        String doctor = null;
        String mayor = null;
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.MAYOR))
            {
                mayor = userThreads.get(i).getData().getUsername() +" is "+ "Mayor";
            }
            else if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
            {
                doctor = userThreads.get(i).getData().getUsername() +" is "+ "CityDoctor";
            }
            if(doctor != null && mayor != null)
            {
                break;
            }
        }
        int count =0;
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.MAYOR))
            {
                count++;
                userThreads.get(i).Receive(doctor);
            }
            else if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
            {
                count++;
                userThreads.get(i).Receive(mayor);
            }
            if(count==2)
            {
                break;
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
    public UserThread GetPlayer(String name)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getUsername().equals(name))
            {
                return userThreads.get(i);
            }
        }
        return null;
    }
    private void GodFather()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.GODFATHER))
            {
                userThreads.get(i).Receive("Choose the civilian you want to kill");
                userThreads.get(i).setChoosePlayerMode(true);
                GodFather temp = (GodFather) userThreads.get(i).getData().getRole();
                while (userThreads.get(i).ChoosePlayer()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                UserThread help = GetPlayer(userThreads.get(i).ChoosePlayer());
                if(help != null )
                {
                    temp.action(help);
                }
                userThreads.get(i).Receive("Done");
                break;
            }
        }
    }
    private void DrLecter()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.LECTER))
            {
                userThreads.get(i).Receive("Choose the Mafia you want to save");
                userThreads.get(i).setChoosePlayerMode(true);
                Lecter temp = (Lecter) userThreads.get(i).getData().getRole();
                while (userThreads.get(i).ChoosePlayer()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                UserThread help = GetPlayer(userThreads.get(i).ChoosePlayer());
                if(help != null )
                {
                    temp.action(help);
                }
                userThreads.get(i).Receive("Done");
                break;
            }
        }
    }
    private void CityDoctor()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
            {
                userThreads.get(i).Receive("Choose the player you want to save");
                userThreads.get(i).setChoosePlayerMode(true);
                CityDoctor temp = (CityDoctor) userThreads.get(i).getData().getRole();
                while (userThreads.get(i).ChoosePlayer()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                UserThread help = GetPlayer(userThreads.get(i).ChoosePlayer());
                if(help != null )
                {
                    temp.action(help);
                }
                userThreads.get(i).Receive("Done");
                break;
            }
        }
    }
    private void Detective()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.DETECTIVE))
            {
                userThreads.get(i).Receive("Choose the player you want to know about");
                userThreads.get(i).setChoosePlayerMode(true);
                Detective temp = (Detective) userThreads.get(i).getData().getRole();
                while (userThreads.get(i).ChoosePlayer()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                UserThread help = GetPlayer(userThreads.get(i).ChoosePlayer());
                if(help != null )
                {
                    userThreads.get(i).Receive(temp.action(help));
                }
                userThreads.get(i).Receive("Done");
                break;
            }
        }
    }
    private void Professional()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.PROFESSIONAL))
            {
                userThreads.get(i).Receive("Do you want to use your ability ?");
                userThreads.get(i).setChoosePlayerMode(true);
                Professional temp = (Professional) userThreads.get(i).getData().getRole();
                while (userThreads.get(i).poll()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                if(userThreads.get(i).poll().equals("Yes"))
                {
                    while (userThreads.get(i).ChoosePlayer()==null)
                    {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    UserThread help = GetPlayer(userThreads.get(i).ChoosePlayer());
                    if(help != null )
                    {
                        temp.action(help);
                    }
                    userThreads.get(i).Receive("Done");
                    break;
                }
                else
                {
                    break;
                }

            }
        }
    }
    private void Psychologist()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.PSYCHOLOGIST))
            {
                userThreads.get(i).Receive("Do you want to use your ability ?");
                userThreads.get(i).setChoosePlayerMode(true);
                Psychologist temp = (Psychologist) userThreads.get(i).getData().getRole();
                while (userThreads.get(i).poll()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                if(userThreads.get(i).poll().equals("Yes"))
                {
                    UserThread help = GetPlayer(userThreads.get(i).ChoosePlayer());
                    this.PsychologistChoice = help;
                    if(help != null )
                    {
                        temp.action(help);
                    }
                    userThreads.get(i).Receive("Done");
                    break;
                }
                else
                {
                    break;
                }
            }
        }
    }
    private void DieHard()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.DIEHARD))
            {
                userThreads.get(i).setChoosePlayerMode(true);
                while (userThreads.get(i).poll()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                if(userThreads.get(i).poll().equals("Yes"))
                {
                    while (!DiehardPermission)
                    {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    DieHard temp = (DieHard) userThreads.get(i).getData().getRole();
                    temp.AnounceRequest();
                    this.anouncement=true;
                    userThreads.get(i).Receive("Done");
                    break;
                }
                else
                {
                    break;
                }
            }
        }
    }
    private void UpdateDead()
    {
          for (int i=0;i<userThreads.size();i++)
          {
              if(!userThreads.get(i).getData().getRole().isAlive())
              {
                  Dead.add(userThreads.get(i));
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
    public void MuteAll()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            userThreads.get(i).getData().getRole().setCanChat(false);
        }
    }
    private void UnMuteAll()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            userThreads.get(i).getData().getRole().setCanChat(true);
        }
    }
    private void UnMuteMafia()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole() instanceof Mafia)
            {
                userThreads.get(i).getData().getRole().setCanChat(true);
            }
        }
    }
    public void RemoveThread(UserThread thread,String mode)
    {
        if(mode.equals("Normal"))
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
        else
        {
            System.err.println("A player disconnected");
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

    }
    public void setDiehardPermission(boolean diehardPermission) {
        DiehardPermission = diehardPermission;
    }
    public boolean isDiehardPermission() {
        return DiehardPermission;
    }
    public void setJoinigFinished(boolean joinigFinished) {
        this.joinigFinished = joinigFinished;
    }
    public boolean isJoinigFinished() {
        return joinigFinished;
    }
    private void CheckTime(int seconds)
    {
        Timer timer = new Timer();
        timer.schedule(new TimeCounter(),0,1000);
        try {
            Thread.sleep(seconds* 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
    }
    private String Ended()
    {
        int countMafia=0;
        int countCivilian=0;
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole() instanceof Mafia)
            {
                countMafia++;
            }
            else
            {
                countCivilian++;
            }
        }
        if(countMafia==0)
        {
            return "Civilians";
        }
        else if(countMafia >= countCivilian)
        {
            return "Mafia";
        }
        else
        {
            return null;
        }
    }
}
