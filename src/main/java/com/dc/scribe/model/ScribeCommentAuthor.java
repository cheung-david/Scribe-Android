package com.dc.scribe.model;

/**
 * Created by david on 18/08/16.
 */
public class ScribeCommentAuthor {
    private String email;
    private String fullName;
    private String id;
    private String profilePic;

    public ScribeCommentAuthor(String email, String fullName, String id, String profilePic) {
        this.email = email;
        this.fullName = fullName;
        this.id = id;
        this.profilePic = profilePic;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getId() {
        return id;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
