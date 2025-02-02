package ru.webapp.serviceapp;

import java.util.List;

public class PageData {
    private String pageUrl;
    private String pageTitle;
    private String pageContent;
    private List<String> imageUrls;

    public PageData(){}

    public PageData(String pageUrl, String pageTitle, String pageContent, List<String> imageUrls) {
        this.pageUrl = pageUrl;
        this.pageTitle = pageTitle;
        this.pageContent = pageContent;
        this.imageUrls = imageUrls;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getPageContent() {
        return pageContent;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}
