package com.dc.scribe.model;

/**
 * Created by david on 25/08/16.
 */
public class ScribeNotification {
    private String _id, to, from, content, action, date, profilePic, commentId;
    private Boolean seen;


    public ScribeNotification(String _id, String to, String from, String content, String action, String date, String profilePic, String commentId, Boolean seen) {
        this._id = _id;
        this.to = to;
        this.from = from;
        this.content = content;
        this.action = action;
        this.date = date;
        this.profilePic = profilePic;
        this.commentId = commentId;
        this.seen = seen;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
}
