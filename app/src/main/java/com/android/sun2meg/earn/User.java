package com.android.sun2meg.earn;
// User.java
public class User {
    private int coins;

    public User() {
        // Empty constructor needed for Firebase
    }

    public User(int coins) {
        this.coins = coins;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}

