package com.company.Logic;
import com.company.Civilians.*;
import com.company.Mafias.GodFather;
import com.company.Mafias.Lecter;
import com.company.Mafias.Mafia;
import com.company.PlayerData;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class is the joint point between players
 * and Server which receives messages from players
 * and send back messages
 */
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
    private boolean ExitMode = false;
    private String  choosenPlayer = null;
    private String MafiaVote = null;
    private String Vote = null;
    private String poll= null;
    private String MayorDecision=null;
    private String Watch = null;
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
    public String RED = "\u001B[31m";
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
     * Constructor
     * Instantiates a new User thread.
     *
     * @param socket client's socket
     * @param server  server
     */
    public UserThread(Socket socket,Server server)
    {
        this.socket = socket;
        this.server=server;
        this.data = new PlayerData();
    }

    /**
     * Main method of this class
     * at first it checks player's username validity
     *then if player is ready ,thread will execute the while(true) loop in which
     * it control voting , night actions, ... modes
     */
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
            //main loop is here
            while (true)
            {

                String message = in.readUTF();
                Thread.sleep(100);
                if(message.equals("Exit"))
                {
                    if(MayorMode)
                    {
                        setMayorDecision("NO");
                    }
                    else if(ChoosePlayerMode)
                    {
                        ChoosePlayerMode =false;
                    }
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
                    }
                }
                else if(VotingMode)
                {
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
                                GodFather temp = (GodFather) this.getData().getRole();
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
                                if(message.equals("Exit"))
                                {
                                    ExitMode = true;
                                    ChoosePlayerMode=false;
                                    break;
                                }
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
                            else if (this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.LECTER))
                            {
                                Lecter temp = (Lecter) this.data.getRole();
                                if(temp.getSelfHeal()==0)
                                {
                                    temp.SelfHeal();
                                    validity= true;
                                    out.writeUTF("Done");
                                    ChoosePlayerMode=false;
                                }
                                else
                                {
                                    out.writeUTF("You have healed yourself once before-Choose someone else");
                                }
                            }
                            else
                            {
                                Lecter temp = (Lecter) this.data.getRole();
                                temp.action(this.server.GetPlayer(message));
                                validity=true;
                                out.writeUTF("Done");
                                ChoosePlayerMode =false;
                            }
                            if(validity)
                            {
                            }
                            else {
                                message=in.readUTF();
                                if(message.equals("Exit"))
                                {
                                    ExitMode = true;
                                    ChoosePlayerMode=false;
                                    break;
                                }
                            }
                        }
                    }
                    else if (this.getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
                    {
                        boolean validity = false;
                        while (!validity)
                        {
                            if(this.server.GetPlayer(message)==null)
                            {
                                out.writeUTF("Not valid player");
                            }
                            else if (this.server.GetPlayer(message).getData().getRole().getCharacter().equals(Position.CITYDOCTOR))
                            {
                                CityDoctor temp = (CityDoctor) this.getData().getRole();
                                if(temp.getSelfHeal()==0)
                                {
                                    temp.SelfHeal();
                                    out.writeUTF("Done");
                                    validity=true;
                                    ChoosePlayerMode=false;
                                }
                                else
                                {
                                    out.writeUTF("You have healed yourself once before-Choose someone else");
                                }
                            }
                            else
                            {
                                CityDoctor temp = (CityDoctor) this.getData().getRole();
                                temp.action(this.server.GetPlayer(message));
                                out.writeUTF("Done");
                                validity=true;
                                ChoosePlayerMode=false;
                            }
                            if(validity)
                            {
                            }
                            else
                            {
                                message=in.readUTF();
                                if(message.equals("Exit"))
                                {
                                    ExitMode = true;
                                    ChoosePlayerMode=false;
                                    break;
                                }
                            }
                        }
                        while (this.server.GetPlayer(message)==null)
                        {
                            out.writeUTF("Not valid player");
                            message = in.readUTF();
                            if(message.equals("Exit"))
                            {
                                ExitMode = true;
                                break;
                            }
                        }
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
                                Detective temp = (Detective)this.data.getRole();
                                out.writeUTF(temp.action(this.server.GetPlayer(message)));
                                ChoosePlayerMode = false;
                                validity=true;
                                out.writeUTF("Done");
                            }
                            if(validity)
                            {
                            }
                            else {
                                message=in.readUTF();
                                if(message.equals("Exit"))
                                {
                                    ExitMode = true;
                                    ChoosePlayerMode=false;
                                    break;
                                }
                            }
                        }
                    }
                    else if(this.getData().getRole().getCharacter().equals(Position.PROFESSIONAL))
                    {
                        if(message.equals("YES"))
                        {
                            out.writeUTF("Choose the player you want to kill");
                            out.writeUTF(this.server.GetAllplayers());
                            message= in.readUTF();
                            if(message.equals("Exit"))
                            {
                                break;
                            }
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
                                    if(message.equals("Exit"))
                                    {
                                        ExitMode = true;
                                        ChoosePlayerMode=false;
                                        break;
                                    }
                                }
                            }
                        }
                        else
                        {
                            ChoosePlayerMode=false;
                        }
                    }
                    else if(this.getData().getRole().getCharacter().equals(Position.PSYCHOLOGIST))
                    {
                        if(message.equals("YES"))
                        {
                            out.writeUTF("Choose the player you want to mute");
                            out.writeUTF(this.server.GetAllplayers());
                            message= in.readUTF();
                            if(message.equals("Exit"))
                            {
                                break;
                            }
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
                                    Psychologist temp = (Psychologist) this.getData().getRole();
                                    this.server.setPsychologistChoice(message);
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
                                    if(message.equals("Exit"))
                                    {
                                        ExitMode = true;
                                        ChoosePlayerMode=false;
                                        break;
                                    }
                                }
                            }
                        }
                        else
                        {
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
                    if(!message.equals("History"))
                    {
                        this.server.SendAll(message,this);
                    }
                }
                if(ExitMode)
                {
                    break;
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

    /**
     * Receive.
     *
     * @param str the str
     */
    public synchronized void Receive(String str)
    {
        try {
            this.out.writeUTF(str);
        }catch (SocketException e)
        {
            System.err.println("Socket is closed");
        }
        catch (IOException exception) {
            System.err.println("Error in Receive");
        }
    }

    /**
     * Ask ready
     * if player is ready at start of the game ,...
     */
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

    /**
     * Choose player string.
     *
     * @return the string
     */
    public synchronized String ChoosePlayer()
    {
            return choosenPlayer;
    }

    /**
     * Mafia vote string.
     *
     * @return the string
     */
    public synchronized String MafiaVote()
    {
        return this.MafiaVote;
    }

    /**
     * Poll string.
     *
     * @return the string
     */
    public synchronized String poll()
    {
        return poll;
    }

    /**
     * Watch string.
     *
     * @return the string
     */
    public String Watch()
    {
        return Watch;
    }

    /**
     * Mayor decision string.
     *
     * @return the string
     */
    public synchronized String MayorDecision()
    {
        return this.MayorDecision;
    }

    /**
     * Sets sleep.
     *
     * @param sleep the sleep
     */
    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    /**
     * Gets sleep.
     *
     * @return the sleep
     */
    public int getSleep() {
        return sleep;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(PlayerData data) {
        this.data = data;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public PlayerData getData() {
        return data;
    }

    /**
     * Is registered boolean.
     *
     * @return the boolean
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Sets registered.
     *
     * @param registered the registered
     */
    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    /**
     * Sets choose player mode.
     *
     * @param choosePlayerMode the choose player mode
     */
    public void setChoosePlayerMode(boolean choosePlayerMode) {
        ChoosePlayerMode = choosePlayerMode;
    }

    /**
     * Sets choosen player.
     *
     * @param choosenPlayer the choosen player
     */
    public void setChoosenPlayer(String choosenPlayer) {
        this.choosenPlayer = choosenPlayer;
    }

    /**
     * Is choose player mode boolean.
     *
     * @return the boolean
     */
    public boolean isChoosePlayerMode() {
        return ChoosePlayerMode;
    }

    /**
     * Sets voting mode.
     *
     * @param votingMode the voting mode
     */
    public void setVotingMode(boolean votingMode) {
        VotingMode = votingMode;
    }

    /**
     * Is voting mode boolean.
     *
     * @return the boolean
     */
    public boolean isVotingMode() {
        return VotingMode;
    }

    /**
     * Sets mafia voting mode.
     *
     * @param mafiaVotingMode the mafia voting mode
     */
    public void setMafiaVotingMode(boolean mafiaVotingMode) {
        MafiaVotingMode = mafiaVotingMode;
    }

    /**
     * Is mafia voting mode boolean.
     *
     * @return the boolean
     */
    public boolean isMafiaVotingMode() {
        return MafiaVotingMode;
    }

    /**
     * Sets mayor mode.
     *
     * @param mayorMode the mayor mode
     */
    public void setMayorMode(boolean mayorMode) {
        MayorMode = mayorMode;
    }

    /**
     * Sets dead mode.
     *
     * @param deadMode the dead mode
     */
    public void setDeadMode(boolean deadMode) {
        DeadMode = deadMode;
    }

    /**
     * Sets poll.
     *
     * @param poll the poll
     */
    public void setPoll(String poll) {
        this.poll = poll;
    }

    /**
     * Sets mafia vote.
     *
     * @param mafiaVote the mafia vote
     */
    public void setMafiaVote(String mafiaVote) {
        MafiaVote = mafiaVote;
    }

    /**
     * Sets vote.
     *
     * @param vote the vote
     */
    public void setVote(String vote) {
        Vote = vote;
    }

    /**
     * Sets mayor decision.
     *
     * @param mayorDecision the mayor decision
     */
    public void setMayorDecision(String mayorDecision) {
        MayorDecision = mayorDecision;
    }

    /**
     * Disconnect the socket.
     */
    public void Disconnect()
    {
        try {
            out.writeUTF("Close");
            socket.close();
            in.close();
            out.close();
            server.RemoveThread(this,"Normal");
        } catch (IOException exception) {
            System.err.println("Error while disconnecting in userthread");
        }
    }

    /**
     * Is mayor mode boolean.
     *
     * @return the boolean
     */
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
