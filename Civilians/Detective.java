package com.company.Civilians;

import com.company.Logic.Action;
import com.company.Logic.Position;
import com.company.Logic.UserThread;
public class Detective extends Civilian {
    public Detective()
    {
        super(Position.DETECTIVE);
    }
    public String action(UserThread ut)
    {
        return ut.getData().getRole().getAnouncement();
    }
}
