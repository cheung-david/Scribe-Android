package com.dc.scribe.model;

/**
 * Created by david on 18/08/16.
 */
public class ScribeComment {
    private String _id;
    private String text;
    private String date;
    private ScribeCommentAuthor author;

    public ScribeComment(String _id, String text, String date, ScribeCommentAuthor commentAuthor) {
        this._id = _id;
        this.text = text;
        this.date = date;
        this.author = commentAuthor;
    }

    public String get_id() {
        return _id;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public ScribeCommentAuthor getCommentAuthor() {
        return author;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCommentAuthor(ScribeCommentAuthor commentAuthor) {
        this.author = commentAuthor;
    }
}
