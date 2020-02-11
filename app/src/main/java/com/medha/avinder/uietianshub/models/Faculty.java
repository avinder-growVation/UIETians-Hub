package com.medha.avinder.uietianshub.models;

/**
 * Created by Lincoln on 18/05/16.
 */
public class Faculty {
    private int id;
    private String branch, name, position, mail, contact, cvLink;

    public Faculty() {
    }

    public Faculty(int id, String branch, String name, String position, String mail, String contact, String cvLink) {
        this.id = id;
        this.branch = branch;
        this.name = name;
        this.position = position;
        this.mail = mail;
        this.contact = contact;
        this.cvLink = cvLink;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCvLink() {
        return cvLink;
    }

    public void setCvLink(String cvLink) {
        this.cvLink = cvLink;
    }
}