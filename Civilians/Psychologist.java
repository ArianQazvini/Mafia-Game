package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

/**
 *  Psychologist.
 */
public class Psychologist extends Civilian implements Action {
    /**
     * Instantiates a new Psychologist.
     */
    public Psychologist()
    {
        super(Position.PSYCHOLOGIST);
    }

    /**
     * psychologist can mute someone
     * @param ut the player who action will be set on
     */
    @Override
    public void action(UserThread ut)
    {
        ut.getData().getRole().setCanChat(false);
    }
}
