package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;
import com.company.Mafias.Mafia;

public class Professional extends Civilian implements Action {
    public Professional()
    {
        super(Position.PROFESSIONAL);
    }
    @Override
    public void action(UserThread ut)
    {
        if(ut.getData().getRole() instanceof Mafia)
        {
            ut.getData().getRole().Shooted();
        }
        else
        {
            this.setGotShot(true);
            this.setAlive(false);
        }
    }
}
