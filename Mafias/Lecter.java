package com.company.Mafias;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;

/**
 * The type Lecter.
 */
public class Lecter extends Mafia implements Action {
    private int SelfHeal=0;

    /**
     * Instantiates a new Lecter.
     */
    public Lecter()
    {
        super(Position.LECTER);
    }

    /**
     * Lecter healing action
     * @param ut the player who action will be set on
     */
    @Override
    public void action(UserThread ut)
    {
       Mafia temp = (Mafia) ut.getData().getRole();
       temp.setShield(true);
    }

    /**
     * Self heal.
     */
    public void SelfHeal() {
        this.setShield(true);
        SelfHeal =1;
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
