package com.company.Logic;

import com.company.Logic.Position;

public class Role {
    private String type;
    private boolean shield;
    private boolean isAlive;
    private boolean canChat;
    private boolean shooted;
    private Position character;
    public Role(String type, Position character) {
        this.type = type;
        this.shield = false;
        this.isAlive = true;
        this.canChat = true;
        this.shooted = false;
        this.character = character;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public boolean isShield() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
    }

    public boolean isAlive() {
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

    public boolean isShooted() {
        return shooted;
    }

    public void setShooted(boolean shot) {
        this.shooted = shot;
    }

    public Position getCharacter() {
        return character;
    }

    public void setCharacter(Position character) {
        this.character = character;
    }
}
