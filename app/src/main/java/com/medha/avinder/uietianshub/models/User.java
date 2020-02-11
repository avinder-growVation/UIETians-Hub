package com.medha.avinder.uietianshub.models;

import java.io.Serializable;

public class User implements Serializable {
    private String name, branchSem, email, oEmail;

    public User(){}

    public User(String name, String branchSem, String email, String oEmail){
        this.name = name;
        this.branchSem = branchSem;
        this.email = email;
        this.oEmail = oEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranchSem() {
        return branchSem;
    }

    public void setBranchSem(String branchSem) {
        this.branchSem = branchSem;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getoEmail() {
        return oEmail;
    }

    public void setoEmail(String oEmail) {
        this.oEmail = oEmail;
    }
}