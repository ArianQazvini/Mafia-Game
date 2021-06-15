package com.company;

import java.util.TimerTask;

/**
 * The type Time counter.
 * Counting seconds
 */
public class TimeCounter extends TimerTask {
    private int a = 1;
    @Override
    public void run()
    {
        System.out.println("Seconds: "+a);
        a++;
    }
}