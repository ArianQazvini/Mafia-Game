package com.company;

import com.company.Logic.Role;

import java.io.Serializable;

public class PlayerData implements Serializable {
    private String username=null;
    private Role role;
    public void setUsername(String username) {
        this.username = username;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public String getUsername() {
        return username;
    }
    public Role getRole() {
        return role;
    }
//    @Override
//    public boolean equals(Object o)
//    {
//
//    }
}
