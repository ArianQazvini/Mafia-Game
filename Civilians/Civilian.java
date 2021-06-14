package com.company.Civilians;

import com.company.Logic.Position;
import com.company.Logic.Role;

/**
 *  Civilian type
 */
public class Civilian extends Role {
    public Civilian(Position position)
    {
        super("Civilian",position,"-");
    }
}
