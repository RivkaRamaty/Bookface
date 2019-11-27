package com.example.kobishpak.hw01.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String name;
    private String email;
    private int totalPurchase;
    private int booksCount;
    private List<String> myBooks = new ArrayList<>();
    private String signupMethod;

    public User() {
        this.name = "Guest";
        this.email = "";
        this.totalPurchase = 0;
        this.booksCount = 0;
        this.signupMethod = "anonymousUser";
    }

    public User(String name, String email, int booksCount, int totalPurchase, List<String> myBooks, String signupMethod) {
        this.name = name;
        this.email = email;
        this.totalPurchase = totalPurchase;
        this.booksCount = booksCount;
        this.myBooks = myBooks;
        this.signupMethod = signupMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }

    public List<String> getMyBooks() {
        return myBooks;
    }

    public void updatePurchaseStatus(int purchaseFee)
    {
        this.booksCount += 1;
        this.totalPurchase += purchaseFee;
    }

    public String getSignupMethod()
    {
        return this.signupMethod;
    }

    public int getTotalPurchase() {
        return this.totalPurchase;
    }

    public int getMyBooksCount() {
        return this.booksCount;
    }

    public int getBooksCount() {
        return booksCount;
    }

//    public static Creator<User> getCREATOR() {
//        return CREATOR;
//    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTotalPurchase(int totalPurchase) {
        this.totalPurchase = totalPurchase;
    }

    public void setBooksCount(int booksCount) {
        this.booksCount = booksCount;
    }

    public void setMyBooks(List<String> myBooks) {
        this.myBooks = myBooks;
    }

    public void setSignupMethod(String signupMethod) {
        this.signupMethod = signupMethod;
    }
}
