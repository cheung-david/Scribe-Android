package com.dc.scribe.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 28/08/16.
 */
public class ScribeFollowers {
    private String userId;
    private Set<String> followers = new HashSet<>();

    public ScribeFollowers(String userId, Set<String> followers) {
        this.userId = userId;
        this.followers = followers;
    }

    public Set<String> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<String> followers) {
        this.followers = followers;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
