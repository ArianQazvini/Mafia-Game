package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;

/**
 * Diehard
 */
public class DieHard extends Civilian  {
    private int AnounceCount=0;
    private boolean Shield = true;

    /**
     * Instantiates a new Die hard.
     */
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

    /**
     * Anounce request.
     */
    public void AnounceRequest()
    {
        AnounceCount++;
    }

    /**
     * Gets anounce count.
     *
     * @return the anounce count
     */
    public int getAnounceCount() {
        return AnounceCount;
    }
}
