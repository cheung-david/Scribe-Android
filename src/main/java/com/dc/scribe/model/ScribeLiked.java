package com.dc.scribe.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 27/08/16.
 */
public class ScribeLiked {
    Set<String> liked = new HashSet<>();

    public ScribeLiked(Set<String> liked) {
        this.liked = liked;
    }

    public Set<String> getLiked() {
        return liked;
    }

    public void setLiked(Set<String> liked) {
        this.liked = liked;
    }
}
