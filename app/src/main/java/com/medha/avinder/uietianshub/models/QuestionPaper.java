package com.medha.avinder.uietianshub.models;

public class QuestionPaper {
    private String title, credits, link, timestamp;
    private int id, majorMinor, pdfImage;

    public QuestionPaper() {
    }

    public QuestionPaper(int id, String title, String link, int majorMinor, String credits, int pdfImage, String timestamp) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.majorMinor = majorMinor;
        this.credits = credits;
        this.pdfImage = pdfImage;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getMajorMinor() {
        return majorMinor;
    }

    public void setMajorMinor(int majorMinor) {
        this.majorMinor = majorMinor;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public int getPdfImage() {
        return pdfImage;
    }

    public void setPdfImage(int pdfImage) {
        this.pdfImage = pdfImage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
