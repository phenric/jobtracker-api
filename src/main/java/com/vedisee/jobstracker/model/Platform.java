package com.vedisee.jobstracker.model;

public enum Platform {
    LINKEDIN("LinkedIn", "https://www.linkedin.com/jobs"),
    INDEED("Indeed", "https://www.indeed.com"),
    GOOGLE_JOBS("Google Jobs", "https://careers.google.com/jobs");

    private final String name;
    private final String url;

    Platform(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
