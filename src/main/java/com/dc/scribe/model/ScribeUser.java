package com.dc.scribe.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 24/08/16.
 */
public class ScribeUser {
    private String _id;
    private String fullName;
    private String profilePic;
    private String description;
    private Set<String> following = new HashSet<>();

    public ScribeUser(String _id, String fullName, String profilePic, String description, Set<String> following) {
        this._id = _id;
        this.fullName = fullName;
        this.profilePic = profilePic;
        this.description = description;
        this.following = following;
    }

    public Set<String> getFollowing() {
        return following;
    }

    public void setFollowing(Set<String> following) {
        this.following = following;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
