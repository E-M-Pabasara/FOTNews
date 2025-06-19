package com.example.fotnews;

public class User {
    private String username;
    private String email;
    private String password;
    private long timestamp;


    public User() {
    }

    public User(String username, String email, String password, long timestamp) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.timestamp = timestamp;
    }


    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public long getTimestamp() {
        return timestamp;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
