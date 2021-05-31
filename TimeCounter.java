package com.company;

import java.util.TimerTask;

public class TimeCounter extends TimerTask {
    private int a = 1;
    @Override
    public void run()
    {
        System.out.println("Seconds: "+a);
        a++;
    }
}