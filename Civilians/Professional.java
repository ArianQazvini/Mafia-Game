package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;
import com.company.Mafias.Mafia;

/**
 * Professional character
 */
public class Professional extends Civilian implements Action {

    /**
     * Instantiates a new Professional.
     */
    public Professional()
    {
        super(Position.PROFESSIONAL);
    }

    /**
     * Professional shooting
     * @param ut the player who action will be set on
     */
    @Override
    public void action(UserThread ut)
    {
        if(ut.getData().getRole() instanceof Mafia)
        {
            ut.getData().getRole().Shooted();
        }
        else
        {
            this.Shooted();
        }
    }
}
