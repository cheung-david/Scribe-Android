package com.dc.scribe.model;

/**
 * Created by david on 24/08/16.
 */
public class ScribeUserList {
    private ScribeUser[] users;

    public ScribeUserList(ScribeUser[] users) {
        this.users = users;
    }

    public ScribeUser[] getUsers() {
        return users;
    }

    public void setUsers(ScribeUser[] users) {
        this.users = users;
    }
}
