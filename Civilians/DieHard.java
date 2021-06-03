package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;

public class DieHard extends Civilian  {
    private int AnounceCount=0;
    private boolean Shield = true;
    public DieHard()
    {
        super(Position.DIEHARD);
    }
    @Override
    public void Shooted()
    {
        if(Shield)
        {
           Shield = false;
        }
        else if (!Shield)
        {
           super.Shooted();
        }
    }
    public void AnounceRequest()
    {
        AnounceCount++;
    }
    public int getAnounceCount() {
        return AnounceCount;
    }
}
