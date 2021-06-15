package com.company.Mafias;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

/**
 * Godfather
 */
public class GodFather extends Mafia implements Action {
    /**
     * Instantiates a new God father.
     */
    public GodFather()
    {
        super(Position.GODFATHER);
        super.setAnouncement("-");
    }

    /**
     * Godfather shooting
     * @param ut the player who action will be set on
     */
    @Override
    public void action(UserThread ut)
    {
        ut.getData().getRole().Shooted();
    }
}
