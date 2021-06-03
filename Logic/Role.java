package com.company.Logic;

import com.company.Logic.Position;

public class Role {
    private String type;
    private String anouncement;
    private boolean GotShot=false;
    private boolean isAlive;
    private boolean canChat;
    private Position character;
    public Role(String type, Position character,String anouncement) {
        this.type = type;
        this.isAlive = true;
        this.canChat = true;
        this.character = character;
        this.anouncement =anouncement;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
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
    public void setAlive(boolean alive) {
        isAlive = alive;
    }
    public boolean isCanChat() {
        return canChat;
    }
    public void setCanChat(boolean canChat) {
        this.canChat = canChat;
    }
    public void Shooted() {
        setGotShot(true);
    }
    public Position getCharacter() {
        return character;
    }

    public void setCharacter(Position character) {
        this.character = character;
    }

    public void setAnouncement(String anouncement) {
        this.anouncement = anouncement;
    }
    public String getAnouncement() {
        return anouncement;
    }
    public void setGotShot(boolean gotShot) {
        GotShot = gotShot;
    }
    public boolean isGotShot() {
        return GotShot;
    }
    public void Heal()
    {
        setGotShot(false);
    }
}
