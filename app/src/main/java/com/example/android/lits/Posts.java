package com.example.android.lits;

@com.google.firebase.database.IgnoreExtraProperties
public class Posts
{
    public String post;
    public String fullname;
    public String time;
    public String uid;
    public String imageUrl;
    public String imagePath;
    public java.util.List<String> imageUrls;
    public java.util.List<String> imagePaths;
    public java.util.List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(java.util.List<String> value) { imageUrls = value; }
    public java.util.List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(java.util.List<String> value) { imagePaths = value; }
    @com.google.firebase.database.Exclude
    public java.util.List<String> photos() {
        return imageUrls != null && !imageUrls.isEmpty() ? imageUrls :
                imageUrl == null || imageUrl.isEmpty() ? java.util.Collections.emptyList() : java.util.Collections.singletonList(imageUrl);
    }
    @com.google.firebase.database.Exclude
    public java.util.List<String> photoPaths() {
        return imagePaths != null && !imagePaths.isEmpty() ? imagePaths :
                imagePath == null ? java.util.Collections.emptyList() : java.util.Collections.singletonList(imagePath);
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String value) { imageUrl = value; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String value) { imagePath = value; }
    // One reaction key per user; missing on older posts means no reactions.
    public java.util.Map<String, String> reactions;

    public java.util.Map<String, String> getReactions() { return reactions; }
    public void setReactions(java.util.Map<String, String> reactions) { this.reactions = reactions; }



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
