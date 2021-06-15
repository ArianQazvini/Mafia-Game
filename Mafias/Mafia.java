package com.company.Mafias;

import com.company.Logic.Position;
import com.company.Logic.Role;

/**
 * The type Mafia.
 */
public class Mafia extends Role {
    private boolean Shield = false;

    /**
     * Instantiates a new Mafia.
     *
     * @param position the position
     */
    public Mafia(Position position)
    {
        super("Mafia",position,"+");
    }

    /**
     * Sets shield.
     *
     * @param shield the shield
     */
    public void setShield(boolean shield) {
        Shield = shield;
    }

    /**
     * Is shield boolean.
     *
     * @return the boolean
     */
    public boolean isShield() {
        return Shield;
    }

    /**
     * Being shooted by professional
     */
    @Override
    public void Shooted()
    {
        if(!Shield)
        {
            super.Shooted();
        }
        else
        {
            Shield = false;
        }
    }
}
