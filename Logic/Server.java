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
import java.util.*;

/**
 * This class is the game's narrator
 * which controls game levels order and also accept players
 * and check their names
 *
 * @author ArianQazvini
 * @version 1.0
 */
public class Server extends Thread {
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
    /**
     * The Reset.
     */
    public  String RESET = "\u001B[0m";
    /**
     * The Black.
     */
    public  String BLACK = "\u001B[30m";
    /**
     * The Red.
     */
    public  String RED = "\u001B[31m";
    /**
     * The Green.
     */
    public  String GREEN = "\u001B[32m";
    /**
     * The Yellow.
     */
    public  String YELLOW = "\u001B[33m";
    /**
     * The Blue.
     */
    public  String BLUE = "\u001B[34m";
    /**
     * The Purple.
     */
    public  String PURPLE = "\u001B[35m";
    /**
     * The Cyan.
     */
    public  String CYAN = "\u001B[36m";
    /**
     * The White.
     */
    public  String WHITE = "\u001B[37m";

    /**
     * Server constructor
     *
     * @param port server
     */
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

    /**
     * fill roles
     * if number of players is more than 10 , it will generate simple_mafia or simple_Civilian
     * @param number  number of players
     *
     * */
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

    /**
     *
     * @return a random role from roles arraylist
     */
    private Role RandomRoll()
    {
        Random random = new Random();
        int choice = random.nextInt(roles.size());
        Role temp = roles.get(choice);
        roles.remove(choice);
        return temp;
    }

    /**
     * Main method of server which accepts players at first
     * then controls night and day mode (roles with action , votings ,chatroom,...)
     */
    @Override
    public void run()
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
            //serverSocket.close();
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
                SendAll(CYAN+"Game is going to start..."+RESET);
                SendAll("---------------------------");
                SendAll(BLUE+"***Introduction Night***"+RESET);
                MuteAll();
                MafiaIntroduce();
                Thread.sleep(300);
                MayortoDrIntroduce();
                Thread.sleep(300);
                ForceSendAll(BLUE+"***Introduction Night finished***"+RESET);
                while (true)
                {
                    ForceSendAll(YELLOW+"***Day time***"+RESET);
                    this.PublicChatMode = true;
                    ForceSendAll(RED+"You have only 60 seconds for chatting"+RESET);
                    UnMuteAll(this.PsychologistChoice);
                    Delay(60);
                    MuteAll();
                    this.PublicChatMode = false;
                    ForceSendAll(PURPLE+"***Voting time***"+RESET);
                    ForceSendAll(PURPLE+"***30 seconds for voting***"+RESET);
                    CreatePoll();
                    Voting();
                    Delay(31);
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
                    ForceSendAll(BLUE+"***Night***"+RESET);
                    UnMuteMafia();
                    SendAll("You have only 40 seconds for chatting");
                    Delay(40);
                    MuteAll();
                    MafiaVoting();
                    Delay(30);
                    GodFather();
                    DrLecter();
                    CityDoctor();
                    Detective();
                    Professional();
                    Thread.sleep(100);
                    Psychologist();
                    Thread.sleep(100);
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
                }
                Close();
                //Voting();
                //Mayor();


        }
        catch (IOException | InterruptedException exception) {
            System.err.println("Error about IO in serverside");
        }
    }

    /**
     * check if sent username from player is unique or not
     *
     * @param username player's name
     * @param data     players data object
     * @return true or false
     */
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

    /**
     * register a player thread in userthreads arraylist
     *
     * @param ut player's thread
     */
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

    /**
     * Check if number of online players is equal to number of excepted players (which is given first when we run server)
     * @return true or false
     */
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

    /**
     *
     * @return Allplayers at the start of the game
     */
    private String AllPlayers()
    {
        StringBuilder stringBuilder = new StringBuilder();
        int j=1;
        for (int i=0;i<userThreads.size();i++)
        {
                stringBuilder.append((j)).append("-").append(userThreads.get(i).getData().getUsername()).append("\n");
                j++;
        }
        return stringBuilder.toString();
    }

    /**
     * if all players are ready , we will send them all players' username
     *
     * @return a string
     */
    public String GetAllplayers()
    {
        if(joinigFinished)
            return AllPlayers();
        else
            return null;
    }

    /**
     * whenever a player says he/she is ready , a counter must be increased
     */
    public synchronized void Ready()
    {
        ready++;
    }

    /**
     * ask each player if they are ready or not (at start of the game)
     * @param num index of player's thread in arraylist
     */
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

    /**
     * ask each player if they are ready or not (at start of the game)
     * create a new thread for each player
     */
    private void AskReady()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            ThreadsAskready(i);
        }
    }

    /**
     * if all players are ready , game will start
     *
     * @return true or false
     */
    public boolean CanStartGame()
    {
        if(ready==userThreads.size())
            return true;
        else
            return false;
    }

    /**
     * Send all a message
     * if public chat mode is on, it will save the message to a file
     *
     * @param string message
     * @param ut     sender
     */
    public synchronized void SendAll(String string,UserThread ut)
    {
        if(!PublicChatMode)
        {
            if(!ut.getData().getRole().isCanChat())
            {
                ut.Receive("You can't send messages");
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
                ut.Receive("You can't send messages");
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
        for (int i=0;i<Watchers.size();i++)
        {
            if(!ut.equals(Watchers.get(i)))
            {
                Watchers.get(i).Receive(BLUE+ut.getData().getUsername()+" : "+string+RESET);
            }
        }
    }

    /**
     * save messages in a file
     * @param message message
     * @param sender sender
     */
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

    /**
     * if player enters 'History', all previous messages will be shown
     *
     * @return a string
     */
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

    /**
     * Mafia must know each other
     */
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

    /**
     *  Mayor must know who the CityDoctor is
     */
    private void MayortoDrIntroduce()
    {
        String doctor = null;
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
            {
                doctor = userThreads.get(i).getData().getUsername() +" is "+ "CityDoctor";
                break;
            }
        }
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.MAYOR))
            {
                userThreads.get(i).Receive(doctor);
                break;
            }
        }
    }

    /**
     * Send all a message (also check if player can chat or not)
     * @param msg message
     */
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

    /**
     * send all a message (without checking player canChat mode )
     * @param msg message
     */
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

    /**
     * Send all a message except a specified player
     * @param msg message
     * @param except specified player
     */
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

    /**
     * Send a message
     * @param msg message
     * @param sender sender
     */
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
            Watchers.get(i).Receive(BLUE+msg+RESET);
        }
    }

    /**
     * Get a player by it's name
     *
     * @param name name
     * @return player user thread
     */
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

    /**
     * Godfather action
     */
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
                    while (userThreads.get(i).isChoosePlayerMode())
                    {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    break;
                }
            }
        }
        else
        {
        }
    }

    /**
     *
     * @return list of all alive mafias
     */
    private String AliveMafia()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Alive Mafia"+"\n");
        for (int i=0;i<userThreads.size();i++)
        {
            if(userThreads.get(i).getData().getRole() instanceof Mafia)
            {
                stringBuilder.append(userThreads.get(i).getData().getUsername()).append(" is ").append(userThreads.get(i).getData().getRole().getCharacter().toString()).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * DrLecter action
     */
    private void DrLecter()
    {
        if(Contains(Position.LECTER))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.LECTER))
                {
                    userThreads.get(i).Receive("Choose the Mafia you want to save");
                    userThreads.get(i).Receive(AliveMafia());
                    userThreads.get(i).setChoosePlayerMode(true);
                    while (userThreads.get(i).isChoosePlayerMode())
                    {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    break;
                }
            }
        }
        else
        {
        }
    }

    /**
     * CityDoctor action
     */
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
                    while (userThreads.get(i).isChoosePlayerMode())
                    {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    break;
                }
            }
        }
        else
        {
        }
    }

    /**
     * Detective action
     */
    private void Detective() {
        if (Contains(Position.DETECTIVE)) {
            for (int i = 0; i < userThreads.size(); i++) {
                if (userThreads.get(i).getData().getRole().getCharacter().equals(Position.DETECTIVE)) {
                    userThreads.get(i).Receive("Choose the player you want to know about");
                    userThreads.get(i).Receive(GetAllplayers());
                    userThreads.get(i).setChoosePlayerMode(true);
                    while (userThreads.get(i).isChoosePlayerMode()) {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    break;
                }
            }
        }
        else
            {
            }

    }

    /**
     * Professional action
     */
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
                    while (userThreads.get(i).isChoosePlayerMode())
                    {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    break;
                }
            }
        }
        else
        {
        }
    }

    /**
     * Psychologist action
     */
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
                    while (userThreads.get(i).isChoosePlayerMode())
                    {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    break;
                }
            }
        }
        else
        {
        }
    }

    /**
     * Diehard action
     */
    private void DieHard()
    {
        if(Contains(Position.DIEHARD))
        {
            for (int i=0;i<userThreads.size();i++)
            {
                if(userThreads.get(i).getData().getRole().getCharacter().equals(Position.DIEHARD)) {
                    userThreads.get(i).Receive("Do you want to use your ability ? YES-NO");
                    userThreads.get(i).setChoosePlayerMode(true);
                    while (userThreads.get(i).isChoosePlayerMode())
                    {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    break;
                }
            }
        }
        else
        {
        }

    }

    /**
     * mafias (except godfather) can have a simple voting to help godfather
     * choose someone to shoot
     * @param index mafia's thread's index
     */
    private void MafiaVotingThread(int index)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                userThreads.get(index).setMafiaVotingMode(true);
                userThreads.get(index).Receive("You got 30 seconds for helping godfather choose someone");
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
                userThreads.get(index).setMafiaVote(null);
            }
        });
        thread.start();
    }

    /**
     * Create a voting thread for each mafia (except godfather)
     */
    private void MafiaVoting()
    {
        if(Contains(Position.GODFATHER) && (Contains(Position.LECTER) || Contains(Position.SIMPLE_MAFIA)))
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

    /**
     * update dead players and remove them from all players arraylist
     */
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

    /**
     * if a player is dead , he can either choose to watch rest of the game or leave completely
     * @param ut
     */
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

    /**
     * if dead player completed it's watch request a counter must be increased
     */
    public synchronized void WatchRequestCompleted()
    {
        this.WatchRequests++;
    }

    /**
     * if all players complete their watch request , it will return true
     * @return true or fasle
     */
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

    /**
     * Create a hashmap for voting
     */
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

    /**
     * Register player's vote(Day mode)
     * @param vote vote
     * @param ut voter
     */
    private synchronized void VoteRegister(String vote, UserThread ut)
    {
        poll.get(vote).add(ut);
    }

    /**
     *
     * @return all players with maximum votes
     */
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

    /**
     * Send voting result for all players
     */
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

    /**
     * kill the player with the most votes after the voting
     */
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

    /**
     * voting thread for each player
     * @param index index of thread
     */
    private void VotingThread(int index)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
               userThreads.get(index).setVotingMode(true);
               userThreads.get(index).Receive("What's your vote?");
               userThreads.get(index).Receive(GetAllplayers());
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
                   userThreads.get(index).setVotingMode(false);
                   ForceSendAll(userThreads.get(index).getData().getUsername()+" vote is : "+userThreads.get(index).poll());
                   userThreads.get(index).setPoll(null);
               }
            }
        });
        thread.start();
    }

    /**
     * create a voting thread for each player
     */
    private void Voting()
    {
       for (int i=0;i<userThreads.size();i++)
       {
           VotingThread(i);
       }
    }

    /**
     * mayor action
     */
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
                    UserThread temp= userThreads.get(i);
                    while (userThreads.get(i).MayorDecision()==null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            System.err.println("InterruptedException");
                        }
                    }
                    if(userThreads.get(i).MayorDecision().equals("YES"))
                    {
                        temp.setMayorDecision(null);
                    }
                    else
                    {
                        VotingKill();
                        UpdateDead();
                        temp.setMayorDecision(null);
                    }
                    break;
                }
            }
        }
        else
        {
            VotingKill();
            UpdateDead();
        }

    }

    /**
     * declaring dead people after night mode
     */
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
            ForceSendAll("Nobody will leave us");
            for (int i=0;i<Watchers.size();i++)
            {
                Watchers.get(i).Receive(BLUE+"Nobody will leave us"+RESET);
            }
        }
        else
        {
            ForceSendAll(dead.toString() + " will leave us");
            for (int i=0;i<Watchers.size();i++)
            {
                Watchers.get(i).Receive(dead.toString() + " will leave us");
            }
        }
    }

    /**
     * if diehard uses it's action , we will send  dead players roles for all players
     */
    private void announcement()
    {
        if(anouncement){
            StringBuilder roles = new StringBuilder();
            for (int i=0;i<Dead.size();i++)
            {
                    roles.append(Dead.get(i).getData().getRole().getCharacter().toString()).append("  ");
            }
            ForceSendAll("Diehard got announcement");
            ForceSendAll(RED+roles.toString()+RESET+"is out");
            for (int i=0;i<Watchers.size();i++)
            {
                Watchers.get(i).Receive(BLUE+"Diehard got announcement"+RESET);
                Watchers.get(i).Receive(RED+roles.toString()+RESET+"is out");
            }
        }
        else
        {
        }
    }
//    public void Mute(String name,int sleep)
//    {
//        for (int i=0;i<userThreads.size();i++)
//        {
//            if(userThreads.get(i).getData().getUsername().equals(name))
//            {
//                userThreads.get(i).Receive("You are Muted");
//                userThreads.get(i).getData().getRole().setCanChat(false);
//                userThreads.get(i).setSleep(sleep);
//            }
//        }
//    }
//    public void UnMute(String name)
//    {
//        for (int i=0;i<userThreads.size();i++)
//        {
//            if(userThreads.get(i).getData().getUsername().equals(name))
//            {
//                userThreads.get(i).getData().getRole().setCanChat(true);
//                userThreads.get(i).setSleep(0);
//
//            }
//        }
//    }

    /**
     * Mute all players
     */
    public void MuteAll()
    {
        for (int i=0;i<userThreads.size();i++)
        {
            userThreads.get(i).getData().getRole().setCanChat(false);
        }
    }

    /**
     * unmute all players except a specified player
     * @param except specified player
     */
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

    /**
     * un mute mafia players
     */
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

    /**
     * remove a thread from allthreads list
     *
     * @param thread thread
     * @param mode   if player himself wants to remove it's thread then it is normal mode             else -> un normal mode
     */
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
                    ForceSendAll(name+" disconnected",thread);
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
                    ForceSendAll(name+" disconnected",thread);
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

    /**
     * set diehardPermission
     *
     * @param diehardPermission diehardPermission
     */
    public void setDiehardPermission(boolean diehardPermission) {
        DiehardPermission = diehardPermission;
    }

    /**
     * getter for diehardPermission
     *
     * @return true or false
     */
    public boolean isDiehardPermission() {
        return DiehardPermission;
    }

    /**
     * setter for joinigFinished
     *
     * @param joinigFinished true or false
     */
    public void setJoinigFinished(boolean joinigFinished) {
        this.joinigFinished = joinigFinished;
    }

    /**
     * Is joinig finished boolean.
     *
     * @return true or false
     */
    public boolean isJoinigFinished() {
        return joinigFinished;
    }

    /**
     * Is public chat mode boolean.
     *
     * @return the boolean
     */
    public boolean isPublicChatMode() {
        return PublicChatMode;
    }

    /**
     * Sets anouncement.
     *
     * @param anouncement the anouncement
     */
    public void setAnouncement(boolean anouncement) {
        this.anouncement = anouncement;
    }

    /**
     * Sets die hard mode.
     *
     * @param dieHardMode the die hard mode
     */
    public void setDieHardMode(boolean dieHardMode) {
        DieHardMode = dieHardMode;
    }

    /**
     * set server's thread on sleep
     * @param seconds sleeping seconds
     */
    private void Delay(int seconds)
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

    /**
     * check if a character is still alive or not
     * @param p given character
     * @return true or false
     */
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

    /**
     *
     * @return the winner
     */
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

    /**
     * close the game
     */
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
