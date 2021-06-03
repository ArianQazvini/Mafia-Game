package com.company.Mafias;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

public class GodFather extends Mafia implements Action {
    public GodFather()
    {
        super(Position.GODFATHER);
        super.setAnouncement("-");
    }
    @Override
    public void action(UserThread ut)
    {
        ut.getData().getRole().Shooted();
    }
}
