package com.playgilround.schedule.client.model;

import android.support.annotation.NonNull;

public class User {

    private String user_name;
    private String nick_name;
    private String birth;
    private String email;
    private String password;

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public void setNickName(String nick_name) {
        this.nick_name = nick_name;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return user_name;
    }

    public String getNickName() {
        return nick_name;
    }

    public String getBirth() {
        return birth;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @NonNull
    @Override
    public String toString() {
        return "{ " +
                " 'user_name' : '" + user_name + "', " +
                " 'nick_name' : '" + nick_name + "', " +
                " 'birth' : '" + birth + "', " +
                " 'email' : '" + email + "', " +
                " 'password' : '" + password + "'" +
                "}";
    }
}