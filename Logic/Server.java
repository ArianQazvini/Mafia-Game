package com.company.Logic;
import com.company.Civilians.*;
import com.company.Mafias.GodFather;
import com.company.Mafias.Lecter;
import com.company.Mafias.Mafia;
import com.company.Mafias.SimpleMafia;
import com.company.PlayerData;
import com.company.TimeCounter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {
    private ArrayList<PlayerData> playersData = new ArrayList<>();
    private ArrayList<UserThread> userThreads = new ArrayList<>();
    private ArrayList<UserThread> Dead = new ArrayList<>();
    private ArrayList<UserThread> Watchers = new ArrayList<>();
    private ArrayList<Role> roles = new ArrayList<>();
    private UserThread PsychologistChoice=null;
    private HashMap<String , ArrayList<UserThread>> poll = new HashMap<>();
    private ServerSocket serverSocket = null;
    private String name;
    private int port;
    private int ready=0;
    private int NowDead = 0;
    private int WatchRequests = 0;
    private boolean joinigFinished=false;
    private boolean anouncement=false;
    private boolean DiehardPermission= false;
    private boolean PublicChatMode = false;
//    private boolean PsychoMode = false;
//    private boolean ProMode = false;
    private boolean DieHardMode = true;
    private File file;
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public Server(int port)
    {

        this.name = "Server1";
        this.port= port;
        this.file = new File("Messages.txt");
        if(this.file.exists())
        {
            this.file.delete();
        }
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
                    Thread.sleep(1500);
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
                Thread.sleep(300);
                while (true)
                {
                    ForceSendAll("***Day Time***");
                    this.PublicChatMode = true;
                    ForceSendAll("You got only 60 seconds for chatting");
                    UnMuteAll(null);
                    CheckTime(60);
                    MuteAll();
                    ForceSendAll("***Voting Time***");
                    CreatePoll();
                    Voting();
                    CheckTime(31);
                    ShowResults();
                    Mayor();
                    if(Ended()!=null)
                    {
                        ForceSendAll("The winner is"+Ended());
                        break;
                    }
                    while (!canResume())
                    {
                        Thread.sleep(1500);
                    }
                    ForceSendAll("***Night***");
                    UnMuteMafia();
                    SendAll("You got only 40 seconds for chatting");
                    CheckTime(40);
                    MuteAll();
                    MafiaVoting();
                    GodFather();
                    DrLecter();
                    CityDoctor();
                    Detective();
                    Professional();
                    Psychologist();
                    DieHard();
                    while (DieHardMode)
                    {
                        Thread.sleep(500);
                    }
                    NightState();
                    UpdateDead();
                    announcement();
                    Thread.sleep(1000);
                    if(Ended()!=null)
                    {
                        ForceSendAll("The winner is"+Ended());
                        break;
                    }
                    while (!canResume())
                    {
                        Thread.sleep(1500);
                    }
                    UnMuteAll(this.PsychologistChoice);
                }
                Close();
                //Voting();
                //Mayor();


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
    public synchronized void SendAll(String string,UserThread ut)
    {
        if(!PublicChatMode)
        {
            if(!ut.getData().getRole().isCanChat())
            {
                ut.Receive("You can't send messages cause you are muted or sleeping");
            }
            else
            {
                ut.Receive("You : "+string+GREEN+"✓✓"+RESET);
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
        else
        {
            Save(string,ut.getData().getUsername());
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
        for (int i=0;i<Watchers.size();i++)
        {
            Watchers.get(i).Receive(ut.getData().getUsername()+" : "+string);
        }
    }
    private synchronized void Save(String message,String sender)
    {
          this.file = new File("Messages.txt");
          try(FileWriter fileWriter = new FileWriter(file,true);BufferedWriter bw = new BufferedWriter(fileWriter)) {
              bw.write(sender + " : "+ message + "\n");
              bw.flush();
          }catch (IOException exception) {
            System.err.println("File not found");
        }
    }
    public String LoadAll()
    {
        StringBuilder stringBuilder = new StringBuilder();
        try(FileReader fileReader = new FileReader(file);BufferedReader br = new BufferedReader(fileReader)) {
            int count;
            char[] buffer = new char[2048];
            while (br.ready())
            {
                count = br.read(buffer);
                stringBuilder.append(new String(buffer,0,count));
            }
            return stringBuilder.toString();
        }catch (IOException e)
        {
            System.err.println("IOError in Load method");
        }
        return stringBuilder.toString();
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
    private synchronized void SendAll(String msg)
    {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().isCanChat())
                {
                    userThreads.get(i).Receive(msg);
                }
            }
        for (int i=0;i<Watchers.size();i++)
        {
            Watchers.get(i).Receive(msg);
        }

    }
    private synchronized void ForceSendAll(String msg)
    {
        for (int i=0;i<userThreads.size();i++)
        {
                userThreads.get(i).Receive(msg);
        }
        for (int i=0;i<Watchers.size();i++)
        {
            Watchers.get(i).Receive(msg);
        }
    }
    private synchronized void ForceSendAll(String msg , UserThread except)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(!userThreads.get(i).equals(except))
            {
                userThreads.get(i).Receive(msg);
            }
        }
        for (int i=0;i<Watchers.size();i++)
        {
            Watchers.get(i).Receive(msg);
        }
    }
    private synchronized void SendMafia(String msg,UserThread sender)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole() instanceof  Mafia && !userThreads.get(i).equals(sender))
            {
                userThreads.get(i).Receive(msg);
            }
        }
        for (int i=0;i<Watchers.size();i++)
        {
            Watchers.get(i).Receive(msg);
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
        if(Contains(Position.GODFATHER))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.GODFATHER))
                {
                    userThreads.get(i).Receive("Choose the civilian you want to kill");
                    userThreads.get(i).Receive(GetAllplayers());
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
        else
        {
        }


    }
    private void DrLecter()
    {
        if(Contains(Position.LECTER))
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
        else
        {
        }
    }
    private void CityDoctor()
    {
        if(Contains(Position.CITYDOCTOR))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
                {
                    userThreads.get(i).Receive("Choose the player you want to save");
                    userThreads.get(i).Receive(GetAllplayers());
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
    }
    private void Detective()
    {
        if(Contains(Position.DETECTIVE))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.DETECTIVE))
                {
                    userThreads.get(i).Receive("Choose the player you want to know about");
                    userThreads.get(i).Receive(GetAllplayers());
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
        else
        {
        }

    }
    private void Professional()
    {
        if(Contains(Position.PROFESSIONAL))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.PROFESSIONAL))
                {
                    userThreads.get(i).Receive("Do you want to use your ability ? YES-NO");
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
                    if(userThreads.get(i).poll().equals("YES"))
                    {
                        userThreads.get(i).Receive(GetAllplayers());
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
        else
        {
        }
    }
    private void Psychologist()
    {
        if(Contains(Position.PSYCHOLOGIST))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.PSYCHOLOGIST))
                {
                    userThreads.get(i).Receive("Do you want to use your ability ? YES-NO");
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
                    if(userThreads.get(i).poll().equals("YES"))
                    {
                        userThreads.get(i).Receive(GetAllplayers());
                        while (userThreads.get(i).ChoosePlayer()==null)
                        {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                System.err.println("InterruptedException");
                            }
                        }
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
        else
        {
        }
    }
    private void DieHard()
    {
        if(Contains(Position.DIEHARD))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.DIEHARD)) {
                    DieHard temp1 = (DieHard) userThreads.get(i).getData().getRole();
                    if (temp1.getAnounceCount() < 2)
                    {
                        userThreads.get(i).Receive("Do you want to use your ability? YES-NO");
                        userThreads.get(i).setChoosePlayerMode(true);
                        while (userThreads.get(i).poll()==null)
                        {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                System.err.println("InterruptedException");
                            }
                        }
                        if(userThreads.get(i).poll().equals("YES"))
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
                            DieHardMode = false;
                            break;
                        }
                        else
                        {
                            DieHardMode = false;
                            break;
                        }
                    }
                    else
                    {
                        userThreads.get(i).Receive("You can't use your ability");
                        break;
                    }
                }
            }
        }
        else
        {
        }

    }
    private void MafiaVotingThread(int index)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                userThreads.get(index).setMafiaVotingMode(true);
                userThreads.get(index).Receive("What's your vote?");
                while (userThreads.get(index).MafiaVote()==null)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                    }
                }
                userThreads.get(index).Receive("Done");
                SendMafia(userThreads.get(index).getData().getUsername()+" vote is: "+userThreads.get(index).MafiaVote(),userThreads.get(index));
            }
        });
        thread.start();
    }
    private void MafiaVoting()
    {
        if(Contains(Position.GODFATHER))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.LECTER) || userThreads.get(i).getData().getRole().getCharacter().equals(Position.SIMPLE_MAFIA))
                {
                    MafiaVotingThread(i);
                }
            }
        }
        else
        {
        }
    }
    private void UpdateDead()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(!userThreads.get(i).getData().getRole().isAlive())
            {
                NowDead++;
            }
        }
          for (int i=0;i<userThreads.size();i++)
          {
              if(!userThreads.get(i).getData().getRole().isAlive())
              {
                  WatchRequest(userThreads.get(i));
                  Dead.add(userThreads.get(i));
              }
          }
          Iterator<UserThread> it = userThreads.iterator();
          while (it.hasNext())
          {
              UserThread temp = it.next();
              if(!temp.getData().getRole().isAlive())
              {
                  it.remove();
              }
          }
    }
    private void WatchRequest(UserThread ut)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ut.setDeadMode(true);
                ut.Receive("Do you want stay and just watch the game? YES-NO");
                while (ut.Watch()==null)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Interruption Error");
                    }
                }
                if(ut.Watch().equals("YES"))
                {
                    ut.Receive("Done");
                    Watchers.add(ut);
                }
            }
        });
        thread.start();
    }
    public synchronized void WatchRequestCompleted()
    {
        this.WatchRequests++;
    }
    private boolean canResume()
    {
        if(WatchRequests == NowDead)
        {
            WatchRequests = 0;
            NowDead = 0;
            return true;
        }
        else
        {
            return false;
        }
    }
    private void CreatePoll()
    {
        if(poll==null)
        {
            for (int i=0;i<userThreads.size();i++)
            {
                ArrayList<UserThread> temp = new ArrayList<>();
                poll.put(userThreads.get(i).getData().getUsername(),temp);
            }
        }
        else
        {
            poll.clear();
            poll= new HashMap<>();
            for (int i=0;i<userThreads.size();i++)
            {
                ArrayList<UserThread> temp = new ArrayList<>();
                poll.put(userThreads.get(i).getData().getUsername(),temp);
            }
        }

    }
    private synchronized void VoteRegister(String vote, UserThread ut)
    {
        poll.get(vote).add(ut);
    }
    private HashMap<String ,Integer> Results()
    {
          HashMap<String,Integer> res = new HashMap<>();
          int max = poll.get(userThreads.get(0).getData().getUsername()).size();
          for (int i=1;i<userThreads.size();i++)
          {
              if(poll.get(userThreads.get(i).getData().getUsername()).size()> max)
              {
                  max = poll.get(userThreads.get(i).getData().getUsername()).size();
              }
          }
          for (int i=0;i<userThreads.size();i++)
          {
             if(poll.get(userThreads.get(i).getData().getUsername()).size()== max)
             {
                 res.put(userThreads.get(i).getData().getUsername(),poll.get(userThreads.get(i).getData().getUsername()).size());
             }
          }
          return res;
    }
    private void ShowResults()
    {
        Set<String> keyset = Results().keySet();
        ArrayList<String> keytemp = new ArrayList<String>(keyset);
        ForceSendAll("Results:");
        for (int i=0;i<Results().size();i++)
        {
            ForceSendAll(keytemp.get(i)+" "+ Results().get(keytemp.get(i)));
        }
    }
    private void VotingKill()
    {
        Set<String> keyset = Results().keySet();
        ArrayList<String> keytemp = new ArrayList<String>(keyset);
        int count =0;
        for (int i=0;i<Results().size();i++)
        {
          if(Results().get(keytemp.get(i))==0)
          {
              count++;
          }
        }
        if(count==Results().size())
        {
            ForceSendAll("NOT VALID VOTING");
        }
        else
        {
            if(Results().size()==1)
            {
                GetPlayer(keytemp.get(0)).getData().getRole().setGotShot(true);
                ForceSendAll(GetPlayer(keytemp.get(0)).getData().getUsername()+ " will leave us");
            }
            else
            {
                Random random = new Random();
                int choice = random.nextInt(Results().size());
                GetPlayer(keytemp.get(choice)).getData().getRole().setAlive(false);
                ForceSendAll(GetPlayer(keytemp.get(choice)).getData().getUsername()+ " will leave us");
            }
        }
    }
    private void VotingThread(int index)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
               userThreads.get(index).setVotingMode(true);
               userThreads.get(index).Receive("What's your vote?");
               if(userThreads.get(index).poll()==null)
               {
                   try {
                       Thread.sleep(30 * 1000);
                   } catch (InterruptedException e) {
                       System.err.println("InterruptedException");
                   }
               }
               if(userThreads.get(index).poll()==null)
               {
                   userThreads.get(index).setVotingMode(false);
                   userThreads.get(index).Receive("Times Up!");
                   ForceSendAll(userThreads.get(index).getData().getUsername()+" didn't vote",userThreads.get(index));
               }
               else
               {
                   VoteRegister(userThreads.get(index).poll(),userThreads.get(index));
                   ForceSendAll(userThreads.get(index).getData().getUsername()+" vote is : "+userThreads.get(index).poll());
               }
            }
        });
        thread.start();
    }
    private void Voting()
    {
       for (int i=0;i<userThreads.size();i++)
       {
           VotingThread(i);
       }
    }
    private void Mayor()
    {
        if(Contains(Position.MAYOR))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.MAYOR))
                {
                    userThreads.get(i).Receive("Do you want to cancel voting ? YES-NO");
                    userThreads.get(i).setMayorMode(true);
                    while (userThreads.get(i).MayorDecision()==null)
                    {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    if(userThreads.get(i).MayorDecision().equals("YES"))
                    {

                    }
                    else
                    {
                        VotingKill();
                        UpdateDead();
                    }
                    break;
                }
            }
        }
        else
        {
        }

    }
    private void NightState()
    {
        StringBuilder dead = new StringBuilder();
        int count =0;
        for (int i=0;i<userThreads.size();i++)
        {
            if(!userThreads.get(i).getData().getRole().isAlive())
            {
                count++;
                dead.append(userThreads.get(i).getData().getUsername()).append(" ");
            }
        }
        if(count==0)
        {
            ForceSendAll("Nobody will left us");
        }
        else
        {
            ForceSendAll(dead.toString() + " will left us");
        }
    }
    private void announcement()
    {
        if(anouncement){
            StringBuilder roles = new StringBuilder();
            for (int i=0;i<Dead.size();i++)
            {
                    roles.append(Dead.get(i).getData().getRole().getCharacter().toString()).append(" , ");
            }
            ForceSendAll("Diehard got announcement");
            ForceSendAll(roles.toString()+" IS OUT");
        }
        else
        {
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
    private void UnMuteAll(UserThread except)
    {
        if(except==null)
        {
            for (int i=0;i<userThreads.size();i++)
            {
                userThreads.get(i).getData().getRole().setCanChat(true);
            }
        }
        else
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).equals(except))
                {
                }
                else
                {
                    userThreads.get(i).getData().getRole().setCanChat(true);
                }
            }
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
    public synchronized void RemoveThread(UserThread thread,String mode)
    {
        if(mode.equals("Normal"))
        {
            System.err.println("Normal player disconnection-Exit command");
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
            System.err.println("UnNormal player disconnection");
            System.err.println("a player disconnected");
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
    public boolean isPublicChatMode() {
        return PublicChatMode;
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
    private boolean Contains(Position p)
    {
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(p))
            {
                return true;
            }
        }
        return false;
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
            return " Civilians";
        }
        else if(countMafia >= countCivilian)
        {
            return " Mafia";
        }
        else
        {
            return null;
        }
    }
    private void Close()
    {
        Iterator<UserThread> it = userThreads.iterator();
        while (it.hasNext())
        {
            UserThread temp = it.next();
            temp.Disconnect();
        }
    }
}
