package com.vedisee.jobstracker.service;

import com.vedisee.jobstracker.model.Job;

import java.util.List;

public interface JobScrapingService {
    List<Job> scrapAllPlatforms();
}
