package com.medha.avinder.uietianshub.models;

/**
 * Created by Avinder on 29-01-2018.
 */

public class Workshop {
    private String name, contentLink, sample1Link, sample2Link, logo;

    public Workshop() {
    }

    public Workshop(String name, String contentLink, String sample1Link, String sample2Link, String logo) {
        this.name = name;
        this.contentLink = contentLink;
        this.sample1Link = sample1Link;
        this.sample2Link = sample2Link;
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentLink() {
        return contentLink;
    }

    public void setContentLink(String contentLink) {
        this.contentLink = contentLink;
    }

    public String getSample1Link() {
        return sample1Link;
    }

    public void setSample1Link(String sample1Link) {
        this.sample1Link = sample1Link;
    }

    public String getSample2Link() {
        return sample2Link;
    }

    public void setSample2Link(String sample2Link) {
        this.sample2Link = sample2Link;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
