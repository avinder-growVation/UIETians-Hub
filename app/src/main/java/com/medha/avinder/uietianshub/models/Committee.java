package com.medha.avinder.uietianshub.models;

/**
 * Created by Avinder on 29-01-2018.
 */

public class Committee {
    private int id;
    private String name, details, coverImage, detailsImage, contact, aboutTeam, webPageLink, otherInfo;

    public Committee() {
    }

    public Committee(int id, String name, String details, String coverImage, String detailsImage, String contact, String aboutTeam, String webPageLink, String otherInfo) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.coverImage = coverImage;
        this.detailsImage = detailsImage;
        this.contact = contact;
        this.aboutTeam = aboutTeam;
        this.webPageLink = webPageLink;
        this.otherInfo = otherInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getDetailsImage() {
        return detailsImage;
    }

    public void setDetailsImage(String detailsImage) {
        this.detailsImage = detailsImage;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAboutTeam() {
        return aboutTeam;
    }

    public void setAboutTeam(String aboutTeam) {
        this.aboutTeam = aboutTeam;
    }

    public String getWebPageLink() {
        return webPageLink;
    }

    public void setWebPageLink(String webPageLink) {
        this.webPageLink = webPageLink;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }
}
