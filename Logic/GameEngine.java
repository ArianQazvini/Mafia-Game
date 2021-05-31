package com.company.Logic;

import com.company.Civilians.*;
import com.company.Mafias.GodFather;
import com.company.Mafias.Lecter;
import com.company.Mafias.SimpleMafia;
import com.company.Player;

import java.util.ArrayList;
import java.util.Random;

public class GameEngine {
    private ArrayList<Player> players;
    private ArrayList<Role> roles;
    private int counter=0;
    public Server server;
//    public GameEngine()
//    {
//        players = new ArrayList<>();
//        roles = new ArrayList<>();
//        // get players from server**
//        if(players.size()==10)
//        {
//              GenerateRolls(0);
//              SetRolls();
//        }
//        else
//        {
//            GenerateRolls(players.size()-10);
//            SetRolls();
//        }
//    }
    private void GenerateRolls(int diff)
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
        roles.add(simpleCivilian);
        //-------------------------------------
        if(diff==0)
        {
        }
        else
        {
             Random random = new Random();
             for (int i=0;i<diff;i++)
             {
                 int choice = random.nextInt(2);
                 if(choice == 1)
                 {
                     roles.add(new SimpleCivilian());
                 }
                 else
                 {
                     roles.add(new SimpleMafia());
                 }
             }
        }
    }
//    private void SetRolls()
//    {
//        Random random = new Random();
//        for (int i=0;i<players.size();i++)
//        {
//            int choice = random.nextInt(roles.size());
//            players.get(i).setRole(roles.get(choice));
//        }
//    }
}
