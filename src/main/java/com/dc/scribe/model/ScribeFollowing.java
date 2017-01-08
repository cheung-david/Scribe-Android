package com.dc.scribe.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 25/08/16.
 */
public class ScribeFollowing {
    private Set<String> following = new HashSet<>();

    public ScribeFollowing(Set<String> following) {
        this.following = following;
    }

    public Set<String> getFollowing() {
        return following;
    }

    public void setFollowing(Set<String>following) {
        this.following = following;
    }
}
