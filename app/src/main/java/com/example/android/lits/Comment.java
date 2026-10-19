package com.example.android.lits;

public class Comment {
    public String uid;
    public String author;
    public String text;
    public Long createdAt;
    public Comment() {}
    public Comment(String uid, String author, String text, Long createdAt) {
        this.uid = uid; this.author = author; this.text = text; this.createdAt = createdAt;
    }
}
