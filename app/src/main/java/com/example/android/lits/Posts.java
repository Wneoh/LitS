package com.example.android.lits;

public class Posts
{
    public String post;
    public String fullname;
    public String time;
    public String uid;



    public Posts(){

    }
    public Posts(String post, String fullname, String time, String uid) {
        this.post=post;
        this.fullname = fullname;
        this.time = time;
        this.uid = uid;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
