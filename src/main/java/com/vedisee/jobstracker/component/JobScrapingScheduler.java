package com.vedisee.jobstracker.component;

import com.vedisee.jobstracker.service.JobScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobScrapingScheduler {
    private final JobScrapingService jobScrapingService;

    @Scheduled(cron = "${job.scraping.cron:0 0 2 * * *}")
    public void scheduleScrapping() {
        log.info("Starting scraping...");

        try {
            jobScrapingService.scrapAllPlatforms();
        } catch (Exception e) {
            log.error("Failed while scheduling the jobs scraping: {}", e.getMessage(), e);
        }
    }
}
