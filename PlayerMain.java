package com.company;

import java.util.Scanner;

public class PlayerMain {
    public static void main(String [] args)
    {
//             Scanner scanner = new Scanner(System.in);
//             System.out.println("Enter game port:");
//             int port = scanner.nextInt();
             Player player = new Player("127.0.0.1",8000);
             player.startPlayer();

    }
}
