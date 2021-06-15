package com.company;

import com.company.Logic.Role;

import java.io.Serializable;

/**
 * Every player must have a username and a role
 * this class contains these fields
 */
public class PlayerData implements Serializable {
    private String username=null;
    private Role role;

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets role.
     *
     * @param role the role
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets role.
     *
     * @return the role
     */
    public Role getRole() {
        return role;
    }
//    @Override
//    public boolean equals(Object o)
//    {
//
//    }
}
