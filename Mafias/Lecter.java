package com.company.Mafias;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

public class Lecter extends Mafia implements Action {
    private int SelfHeal=0;
    public Lecter()
    {
        super(Position.LECTER);
    }
    @Override
    public void action(UserThread ut)
    {
       Mafia temp = (Mafia) ut.getData().getRole();
       temp.setShield(true);
    }
    public void SelfHeal() {
        SelfHeal =1;
    }
    public int getSelfHeal() {
        return SelfHeal;
    }
}
