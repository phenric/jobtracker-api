package com.vedisee.jobstracker;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.Platform;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.repository.JobRepository;
import com.vedisee.jobstracker.repository.SearchCriteriaRepository;
import com.vedisee.jobstracker.service.JobScrapingServiceImpl;
import com.vedisee.jobstracker.service.scraper.JobScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobScrapingServiceTest {

    @Mock
    private JobScraperService scraperService1;

    @Mock
    private JobScraperService scraperService2;

    @Mock
    private SearchCriteriaRepository searchCriteriaRepository;

    @Mock
    private JobRepository jobRepository;

    private JobScrapingServiceImpl jobScrapingService;

    private SearchCriteria searchCriteria;
    private Job job1;
    private Job job2;
    private Job job3;

    private Platform platform1;
    private Platform platform2;

    @BeforeEach
    void setUp() {
        reset(scraperService1, scraperService2, searchCriteriaRepository, jobRepository);

        try {
            platform1 = Platform.values()[0];
            platform2 = Platform.values().length > 1 ? Platform.values()[1] : Platform.values()[0];
        } catch (Exception e) {
            platform1 = Platform.LINKEDIN;
            platform2 = Platform.INDEED;
        }

        List<JobScraperService> scraperServices = Arrays.asList(scraperService1, scraperService2);
        jobScrapingService = new JobScrapingServiceImpl(
                scraperServices,
                searchCriteriaRepository,
                jobRepository
        );

        searchCriteria = SearchCriteria.builder()
                .id(1L)
                .keywords(Collections.singleton("Java Developer"))
                .build();

        job1 = createJob("ext-1", "Job 1");
        job2 = createJob("ext-2", "Job 2");
        job3 = createJob("ext-3", "Job 3");
    }

    @Test
    void scrapAllPlatforms_WhenNoSearchCriteria_ShouldReturnEmptyList() {
        // Given
        when(searchCriteriaRepository.findAll()).thenReturn(List.of());

        // When
        List<Job> result = jobScrapingService.scrapAllPlatforms();

        // Then
        assertThat(result).isEmpty();
        verify(searchCriteriaRepository).findAll();
        verifyNoInteractions(scraperService1, scraperService2, jobRepository);
    }

    @Test
    void scrapAllPlatforms_WhenScraperNotAvailable_ShouldSkipScraper() {
        // Given
        when(searchCriteriaRepository.findAll()).thenReturn(List.of(searchCriteria));
        when(scraperService1.isAvailable()).thenReturn(false);
        when(scraperService1.getPlatform()).thenReturn(platform1);
        when(scraperService2.isAvailable()).thenReturn(false);
        when(scraperService2.getPlatform()).thenReturn(platform2);

        // When
        List<Job> result = jobScrapingService.scrapAllPlatforms();

        // Then
        assertThat(result).isEmpty();
        verify(scraperService1).isAvailable();
        verify(scraperService2).isAvailable();
        verify(scraperService1, never()).scrapeJobs(any());
        verify(scraperService2, never()).scrapeJobs(any());
    }

    @Test
    void scrapAllPlatforms_WithCriteriaAndAvailableScrapers_ShouldReturnOnlyNewJobs() {
        // Given
        when(searchCriteriaRepository.findAll()).thenReturn(List.of(searchCriteria));

        when(scraperService1.isAvailable()).thenReturn(true);
        when(scraperService1.getPlatform()).thenReturn(platform1);
        when(scraperService1.scrapeJobs(searchCriteria)).thenReturn(Arrays.asList(job1, job2));

        when(scraperService2.isAvailable()).thenReturn(true);
        when(scraperService2.getPlatform()).thenReturn(platform2);
        when(scraperService2.scrapeJobs(searchCriteria)).thenReturn(List.of(job3));

        when(jobRepository.findByExternalId("ext-1")).thenReturn(Optional.of(job1));
        when(jobRepository.findByExternalId("ext-2")).thenReturn(Optional.empty());
        when(jobRepository.findByExternalId("ext-3")).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Job> result = jobScrapingService.scrapAllPlatforms();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(job2, job3);
        verify(scraperService1).scrapeJobs(searchCriteria);
        verify(scraperService2).scrapeJobs(searchCriteria);
        verify(jobRepository, times(2)).save(any(Job.class));
        verify(jobRepository).save(job2);
        verify(jobRepository).save(job3);
        verify(jobRepository, never()).save(job1);
    }

    @Test
    void scrapAllPlatforms_WhenScraperThrowsException_ShouldContinueWithOtherScrapers() {
        // Given
        when(searchCriteriaRepository.findAll()).thenReturn(List.of(searchCriteria));

        when(scraperService1.isAvailable()).thenReturn(true);
        when(scraperService1.getPlatform()).thenReturn(platform1);
        when(scraperService1.scrapeJobs(searchCriteria))
                .thenThrow(new RuntimeException("Scraping failed"));

        when(scraperService2.isAvailable()).thenReturn(true);
        when(scraperService2.getPlatform()).thenReturn(platform2);
        when(scraperService2.scrapeJobs(searchCriteria)).thenReturn(List.of(job3));

        when(jobRepository.findByExternalId("ext-3")).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Job> result = jobScrapingService.scrapAllPlatforms();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(job3);
        verify(scraperService1).scrapeJobs(searchCriteria);
        verify(scraperService2).scrapeJobs(searchCriteria);
        verify(jobRepository).save(job3);
    }

    @Test
    void scrapAllPlatforms_WithMultipleCriteria_ShouldScrapeForEachCriteria() {
        // Given
        SearchCriteria criteria1 = SearchCriteria.builder()
                .id(1L)
                .keywords(Collections.singleton("Java Developer"))
                .build();

        SearchCriteria criteria2 = SearchCriteria.builder()
                .id(2L)
                .keywords(Collections.singleton("Python Developer"))
                .build();

        when(searchCriteriaRepository.findAll()).thenReturn(Arrays.asList(criteria1, criteria2));

        when(scraperService1.isAvailable()).thenReturn(true);
        when(scraperService1.getPlatform()).thenReturn(platform1);
        when(scraperService1.scrapeJobs(criteria1)).thenReturn(List.of(job1));
        when(scraperService1.scrapeJobs(criteria2)).thenReturn(List.of(job2));

        when(scraperService2.isAvailable()).thenReturn(false);
        when(scraperService2.getPlatform()).thenReturn(platform2);

        when(jobRepository.findByExternalId(anyString())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Job> result = jobScrapingService.scrapAllPlatforms();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(job1, job2);
        verify(scraperService1).scrapeJobs(criteria1);
        verify(scraperService1).scrapeJobs(criteria2);
        verify(jobRepository, times(2)).save(any(Job.class));
    }

    @Test
    void scrapAllPlatforms_WhenScraperReturnsEmptyList_ShouldReturnEmptyList() {
        // Given
        when(searchCriteriaRepository.findAll()).thenReturn(List.of(searchCriteria));
        when(scraperService1.isAvailable()).thenReturn(true);
        when(scraperService1.getPlatform()).thenReturn(platform1);
        when(scraperService1.scrapeJobs(searchCriteria)).thenReturn(List.of());
        when(scraperService2.isAvailable()).thenReturn(true);
        when(scraperService2.getPlatform()).thenReturn(platform2);
        when(scraperService2.scrapeJobs(searchCriteria)).thenReturn(List.of());

        // When
        List<Job> result = jobScrapingService.scrapAllPlatforms();

        // Then
        assertThat(result).isEmpty();
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void scrapAllPlatforms_WhenAllJobsAlreadyExist_ShouldReturnEmptyList() {
        // Given
        when(searchCriteriaRepository.findAll()).thenReturn(List.of(searchCriteria));
        when(scraperService1.isAvailable()).thenReturn(true);
        when(scraperService1.getPlatform()).thenReturn(platform1);
        when(scraperService1.scrapeJobs(searchCriteria)).thenReturn(Arrays.asList(job1, job2));

        when(jobRepository.findByExternalId("ext-1")).thenReturn(Optional.of(job1));
        when(jobRepository.findByExternalId("ext-2")).thenReturn(Optional.of(job2));

        when(scraperService2.isAvailable()).thenReturn(false);
        when(scraperService2.getPlatform()).thenReturn(platform2);

        // When
        List<Job> result = jobScrapingService.scrapAllPlatforms();

        // Then
        assertThat(result).isEmpty();
        verify(jobRepository, never()).save(any(Job.class));
    }

    private Job createJob(String externalId, String title) {
        Job job = new Job();
        job.setExternalId(externalId);
        job.setTitle(title);
        return job;
    }
}