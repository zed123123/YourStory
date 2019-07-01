package com.example.yourstory;

import com.google.firebase.Timestamp;

public class BlogPost extends BlogPostId{

    public String userId, blogImageUrl, blogTxt, blogImageThumbnail;
    public Timestamp timestamp;

    public BlogPost(){}

    public BlogPost(String userId, String blogImageUrl, String blogTxt, String blogImageThumbnail, Timestamp timestamp) {
        this.userId = userId;
        this.blogImageUrl = blogImageUrl;
        this.blogTxt = blogTxt;
        this.blogImageThumbnail = blogImageThumbnail;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userName) {
        this.userId = userName;
    }

    public String getBlogImageUrl() {
        return blogImageUrl;
    }

    public void setBlogImageUrl(String blogImageUrl) {
        this.blogImageUrl = blogImageUrl;
    }

    public String getBlogTxt() {
        return blogTxt;
    }

    public void setBlogTxt(String blogTxt) {
        this.blogTxt = blogTxt;
    }

    public String getBlogImageThumbnail() {
        return blogImageThumbnail;
    }

    public void setBlogImageThumbnail(String blogImageThumbnail) {
        this.blogImageThumbnail = blogImageThumbnail;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
