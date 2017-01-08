package com.dc.scribe.model;

import java.io.Serializable;

/**
 * Created by david on 18/08/16.
 */
public class ScribeCard {
    private String _id;
    private String filename;
    private int likes;
    private String date;
    private String authorId;
    private String authorName;
    private String authorPic;
    private ScribeComment[] comments;

    public ScribeCard(String _id, String filename, int likes, String date, String authorId, String authorName, String authorPic, ScribeComment[] comments) {
        this._id = _id;
        this.filename = filename;
        this.likes = likes;
        this.date = date;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorPic = authorPic;
        this.comments = comments;
    }

    public String get_id() {
        return _id;
    }

    public String getFilename() {
        return filename;
    }

    public int getLikes() {
        return likes;
    }

    public String getDate() {
        return date;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorPic() {
        return authorPic;
    }

    public ScribeComment[] getComments() {
        return comments;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorPic(String authorPic) {
        this.authorPic = authorPic;
    }

    public void setComments(ScribeComment[] comments) {
        this.comments = comments;
    }
}
