package com.vedisee.jobstracker.service;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.repository.JobRepository;
import com.vedisee.jobstracker.repository.SearchCriteriaRepository;
import com.vedisee.jobstracker.service.scraper.JobScraperService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobScrapingServiceImpl implements JobScrapingService {

    private final List<JobScraperService> scraperServices;
    private final SearchCriteriaRepository searchCriteriaRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional
    public List<Job> scrapAllPlatforms() {
        log.info("Starting scraping ...");
        List<SearchCriteria> searchCriteriaList = searchCriteriaRepository.findAll();

        if (searchCriteriaList.isEmpty()) {
            log.warn("No criteria found");
            return List.of();
        }

        List<Job> jobs = new ArrayList<>();

        for (SearchCriteria criteria: searchCriteriaList) {
            log.info("Scraping for criteria: {}", criteria);
            for (JobScraperService scraper: scraperServices) {
                log.info("Scraping for plateform: {}", scraper.getPlatform());
                if (!scraper.isAvailable()) {
                    log.warn("Plateform {} is not reachable", scraper.getPlatform());
                    continue;
                }
                try {
                    List<Job> newJobs = filterAndSaveNewJobs(scraper.scrapeJobs(criteria));
                    jobs.addAll(newJobs);
                    log.info("{} new jobs have been scraped from {}", newJobs.size(), scraper.getPlatform());
                } catch (Exception e) {
                    log.error("Error while scraping {} with message: {}", scraper.getPlatform(), e.getMessage(), e);
                }
            }
        }
        return jobs;
    }

    private List<Job> filterAndSaveNewJobs(List<Job> jobs) {
        List<Job> newJobs = new ArrayList<>();
        for (Job job: jobs) {
            if (jobRepository.findByExternalId(job.getExternalId()).isEmpty()) {
                jobRepository.save(job);
                newJobs.add(job);
            }
        }
        return newJobs;
    }
}
