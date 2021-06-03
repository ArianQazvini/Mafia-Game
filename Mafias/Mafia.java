package com.company.Mafias;

import com.company.Logic.Position;
import com.company.Logic.Role;

public class Mafia extends Role {
    private boolean Shield = false;
    public Mafia(Position position)
    {
        super("Mafia",position,"+");
    }
    public void setShield(boolean shield) {
        Shield = shield;
    }
    public boolean isShield() {
        return Shield;
    }
    @Override
    public void Shooted()
    {
        if(!Shield)
        {
            super.Shooted();
        }
        else
        {
            Shield = true;
        }
    }
}
