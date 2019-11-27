package com.example.kobishpak.hw01.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Review {
    private String userReview;
    private int userRating;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String userName;

    public Review(String userReview, int userRating, String userName) {
        this.userReview = userReview;
        this.userRating = userRating;
        this.userName = userName;
    }

    public Review() {
    }

    public String getUserReview() {
        return userReview;
    }

    public int getUserRating() {
        return userRating;
    }



    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("userReview", userReview);
        result.put("userRating", userRating);
        result.put("userName", userName);
        return result;
    }

}
