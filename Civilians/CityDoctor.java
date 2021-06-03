package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

public class CityDoctor extends Civilian implements Action {
    private int SelfHeal = 0;
    public CityDoctor()
    {
        super(Position.CITYDOCTOR);
    }
    @Override
    public void action(UserThread ut)
    {
       if(ut.getData().getRole() instanceof Civilian)
       {
           if(ut.getData().getRole().isGotShot())
           {
               ut.getData().getRole().Heal();
           }
       }
    }
    public void SelfHeal() {
        SelfHeal = 1;
    }
    public int getSelfHeal() {
        return SelfHeal;
    }
}
