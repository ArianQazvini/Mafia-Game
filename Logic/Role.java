package com.company.Logic;

import com.company.Logic.Position;

/**
 * Role class (indicate each character)
 */
public class Role {
    private String type;
    private String anouncement;
    private boolean GotShot=false;
    private boolean isAlive;
    private boolean canChat;
    private Position character;

    /**
     * Instantiates a new Role.
     *
     * @param type        the type
     * @param character   the character
     * @param anouncement the anouncement
     */
    public Role(String type, Position character,String anouncement) {
        this.type = type;
        this.isAlive = true;
        this.canChat = true;
        this.character = character;
        this.anouncement =anouncement;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Is alive boolean.
     *
     * @return the boolean
     */
    public boolean isAlive() {
        if(isGotShot())
        {
            isAlive=false;
        }
        else
        {
            isAlive= true;
        }
        return isAlive;
    }

    /**
     * Sets alive.
     *
     * @param alive the alive
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    /**
     * Is can chat boolean.
     *
     * @return the boolean
     */
    public boolean isCanChat() {
        return canChat;
    }

    /**
     * Sets can chat.
     *
     * @param canChat the can chat
     */
    public void setCanChat(boolean canChat) {
        this.canChat = canChat;
    }

    /**
     * Shooted.
     */
    public void Shooted() {
        setGotShot(true);
    }

    /**
     * Gets character.
     *
     * @return the character
     */
    public Position getCharacter() {
        return character;
    }

    /**
     * Sets character.
     *
     * @param character the character
     */
    public void setCharacter(Position character) {
        this.character = character;
    }

    /**
     * Sets anouncement.
     *
     * @param anouncement the anouncement
     */
    public void setAnouncement(String anouncement) {
        this.anouncement = anouncement;
    }

    /**
     * Gets anouncement.
     *
     * @return the anouncement
     */
    public String getAnouncement() {
        return anouncement;
    }

    /**
     * Sets got shot.
     *
     * @param gotShot the got shot
     */
    public void setGotShot(boolean gotShot) {
        GotShot = gotShot;
    }

    /**
     * Is got shot boolean.
     *
     * @return the boolean
     */
    public boolean isGotShot() {
        return GotShot;
    }

    /**
     * Heal.
     */
    public void Heal()
    {
        setGotShot(false);
    }
}
