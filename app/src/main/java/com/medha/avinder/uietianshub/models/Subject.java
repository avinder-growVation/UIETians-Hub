package com.medha.avinder.uietianshub.models;

public class Subject {
    private String branchSem, title, link, page;

    public Subject() {
    }

    public Subject(String branchSem, String title, String link, String page) {
        this.branchSem = branchSem;
        this.title = title;
        this.link = link;
        this.page = page;
    }

    public String getBranchSem() {
        return branchSem;
    }

    public void setBranchSem(String branchSem) {
        this.branchSem = branchSem;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }
}
