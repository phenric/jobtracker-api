package com.vedisee.jobstracker;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.Platform;
import com.vedisee.jobstracker.service.JobScrapingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableScheduling
@DirtiesContext
@TestPropertySource(properties = {
        "job.scraping.cron=*/2 * * * * *"
})
class JobScrapingSchedulerTest {

    @MockitoBean
    private JobScrapingService jobScrapingService;

    @Test
    void shouldExecuteScheduledJobSuccessfully() {
        // Given
        Job job = Job.builder()
                .externalId(Platform.INDEED.getName() + "-" + UUID.randomUUID())
                .title("Software engineer - Position 0")
                .company("Company 0")
                .description("Job description for software engineer")
                .url(Platform.INDEED.getUrl() + "/" + UUID.randomUUID())
                .platform(Platform.INDEED)
                .build();

        // When
        // Then
        when(jobScrapingService.scrapAllPlatforms()).thenReturn(List.of(job));
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        verify(jobScrapingService, atLeastOnce()).scrapAllPlatforms()
                );
    }

    @Test
    void shouldHandleExceptionDuringScheduledExecution() {
        // Given
        doThrow(new RuntimeException("Scraping failed"))
                .when(jobScrapingService).scrapAllPlatforms();

        // When
        // Then
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        verify(jobScrapingService, atLeastOnce()).scrapAllPlatforms()
                );
    }
}
