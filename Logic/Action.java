package com.company.Logic;

/**
 * The interface Action.
 */
public interface Action {
    /**
     * Characters actions
     *
     * @param ut the player who action will be set on
     */
    public abstract void action(UserThread ut);
}
