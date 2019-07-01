package com.example.yourstory;

public class User {

    public String userName;
    public String userImage;
    public String userBio;

    public User(){}

    public User(String userName, String userImage, String userBio) {
        this.userName = userName;
        this.userImage = userImage;
        this.userBio = userBio;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

}
