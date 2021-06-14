package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

/**
 *  Detective.
 */
public class Detective extends Civilian {
    /**
     * Instantiates a new Detective.
     */
    public Detective()
    {
        super(Position.DETECTIVE);
    }

    /**
     * get a player type (- for civilians and godfather, + for mafias)
     *
     * @param ut player
     * @return type
     */
    public String action(UserThread ut)
    {
        return ut.getData().getRole().getAnouncement();
    }
}
