package com.vedisee.jobstracker.service.scraper;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.Platform;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.service.SiteAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LinkedInScraperService implements JobScraperService {

    private final Environment env;

    private final SiteAvailabilityService siteAvailabilityService;

    @Override
    public Platform getPlatform() {
        return Platform.LINKEDIN;
    }

    @Override
    public List<Job> scrapeJobs(SearchCriteria searchCriteria) {
        log.info("Scraping LinkedIn with criteria: {}", searchCriteria.getName());

        List<Job> jobs = new ArrayList<>();

        try {
            for (String keyword: searchCriteria.getKeywords()) {
                for (String location: searchCriteria.getLocations()) {
                    List<Job> scraped = scrapeByKeywordsAndLocation(keyword, location);
                    jobs.addAll(scraped);
                }
            }
            log.info("Scraping LinkedIn done => {}", jobs.size());
        } catch (Exception e) {
            log.error("Error scraping LinkedIn: {}", e.getMessage(), e);
        }

        return jobs;
    }

    @Override
    public boolean isAvailable() {
        return siteAvailabilityService.isSiteReachable(Platform.LINKEDIN.getUrl());
    }

    private List<Job> scrapeByKeywordsAndLocation(String keyword, String location) {
        List<Job> jobs = new ArrayList<>();

        if (env.acceptsProfiles(Profiles.of("dev"))) {
            for (int i = 0; i < 3; i++) {
                Job job = Job.builder()
                        .externalId(Platform.LINKEDIN.getName() + "-" + UUID.randomUUID())
                        .title(keyword + " - Position " + (i + 1))
                        .company("Company " + (i + 1))
                        .description("Job description for " + keyword + " in " + location)
                        .url(Platform.LINKEDIN.getUrl() + "/" + UUID.randomUUID())
                        .platform(Platform.LINKEDIN)
                        .build();

                jobs.add(job);
            }
        }
        // TODO: implement the real API to scrap LinkedIn

        return jobs;
    }
}
