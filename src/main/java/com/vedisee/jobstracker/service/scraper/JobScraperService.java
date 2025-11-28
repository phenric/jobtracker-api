package com.vedisee.jobstracker.service.scraper;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.Platform;
import com.vedisee.jobstracker.model.SearchCriteria;

import java.util.List;

public interface JobScraperService {
    Platform getPlatform();

    List<Job> scrapeJobs(SearchCriteria searchCriteria);

    boolean isAvailable();
}
