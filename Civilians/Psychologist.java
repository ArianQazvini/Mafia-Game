package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

public class Psychologist extends Civilian implements Action {
    public Psychologist()
    {
        super(Position.PSYCHOLOGIST);
    }
    @Override
    public void action(UserThread ut)
    {
        ut.getData().getRole().setCanChat(false);
    }
}
