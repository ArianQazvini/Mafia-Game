package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

/**
 * CityDoctor
 */
public class CityDoctor extends Civilian implements Action {
    private int SelfHeal = 0;

    /**
     * Instantiates a new City doctor.
     */
    public CityDoctor()
    {
        super(Position.CITYDOCTOR);
    }

    /**
     * Citydoctor special action - can save a person who had been shot
     * @param ut the player who action will be set on
     */
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

    /**
     * Self heal.
     */
    public void SelfHeal() {
        SelfHeal = 1;
    }

    /**
     * Gets self heal.
     *
     * @return the self heal
     */
    public int getSelfHeal() {
        return SelfHeal;
    }
}
