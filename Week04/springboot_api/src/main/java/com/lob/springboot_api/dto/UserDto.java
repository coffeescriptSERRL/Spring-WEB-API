package com.lob.springboot_api.entity;

public class UserDto {

    private String userID;
    private String hr_Organ;
    private String username;
    private String password;

    public String getUserID() {
        return userID;
    }

    public String getHr_Organ() {
        return hr_Organ;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHr_Organ(String hr_Organ) {
        this.hr_Organ = hr_Organ;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}