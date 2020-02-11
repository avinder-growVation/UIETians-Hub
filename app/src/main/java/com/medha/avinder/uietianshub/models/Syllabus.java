package com.medha.avinder.uietianshub.models;

public class Syllabus {
    private String branchSem, subjects, pages, link;

    public Syllabus() {
    }

    public Syllabus(String branchSem, String subjects, String pages, String link) {
        this.branchSem = branchSem;
        this.subjects = subjects;
        this.pages = pages;
        this.link = link;
    }

    public String getBranchSem() {
        return branchSem;
    }

    public void setBranchSem(String branchSem) {
        this.branchSem = branchSem;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
