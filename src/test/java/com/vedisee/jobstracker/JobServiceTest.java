package com.vedisee.jobstracker;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.repository.JobRepository;
import com.vedisee.jobstracker.repository.SearchCriteriaRepository;
import com.vedisee.jobstracker.service.JobScrapingService;
import com.vedisee.jobstracker.service.JobServiceImpl;
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
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SearchCriteriaRepository searchCriteriaRepository;

    @Mock
    private JobScrapingService jobScrapingService;

    private JobServiceImpl jobService;

    private Job job1;
    private Job job2;
    private SearchCriteria searchCriteria1;
    private SearchCriteria searchCriteria2;

    @BeforeEach
    void setUp() {
        jobService = new JobServiceImpl(
                jobRepository,
                searchCriteriaRepository,
                jobScrapingService
        );

        job1 = createJob(1L, "ext-1", "Java Developer", "Company A");
        job2 = createJob(2L, "ext-2", "Python Developer", "Company B");

        searchCriteria1 = SearchCriteria.builder()
                .id(1L)
                .keywords(Collections.singleton("Java Developer"))
                .locations(Collections.singleton("Remote"))
                .build();

        searchCriteria2 = SearchCriteria.builder()
                .id(2L)
                .keywords(Collections.singleton("Python Developer"))
                .locations(Collections.singleton("New York"))
                .build();
    }

    @Test
    void findAllJobs_WhenJobsExist_ShouldReturnAllJobs() {
        // Given
        List<Job> expectedJobs = Arrays.asList(job1, job2);
        when(jobRepository.findAll()).thenReturn(expectedJobs);

        // When
        List<Job> result = jobService.findAllJobs();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(job1, job2);
        verify(jobRepository).findAll();
    }

    @Test
    void findAllJobs_WhenNoJobsExist_ShouldReturnEmptyList() {
        // Given
        when(jobRepository.findAll()).thenReturn(List.of());

        // When
        List<Job> result = jobService.findAllJobs();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(jobRepository).findAll();
    }

    @Test
    void findJobById_WhenJobExists_ShouldReturnJob() {
        // Given
        Long jobId = 1L;
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job1));

        // When
        Optional<Job> result = jobService.findJobById(jobId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(job1);
        assertThat(result.get().getId()).isEqualTo(jobId);
        assertThat(result.get().getTitle()).isEqualTo("Java Developer");
        verify(jobRepository).findById(jobId);
    }

    @Test
    void findJobById_WhenJobDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long jobId = 999L;
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // When
        Optional<Job> result = jobService.findJobById(jobId);

        // Then
        assertThat(result).isEmpty();
        verify(jobRepository).findById(jobId);
    }

    @Test
    void findJobById_WithNullId_ShouldReturnEmpty() {
        // Given
        when(jobRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Job> result = jobService.findJobById(null);

        // Then
        assertThat(result).isEmpty();
        verify(jobRepository).findById(null);
    }

    @Test
    void findAllSearchCriteria_WhenCriteriaExist_ShouldReturnAllCriteria() {
        // Given
        List<SearchCriteria> expectedCriteria = Arrays.asList(searchCriteria1, searchCriteria2);
        when(searchCriteriaRepository.findAll()).thenReturn(expectedCriteria);

        // When
        List<SearchCriteria> result = jobService.findAllSearchCriteria();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(searchCriteria1, searchCriteria2);
        verify(searchCriteriaRepository).findAll();
    }

    @Test
    void findAllSearchCriteria_WhenNoCriteriaExist_ShouldReturnEmptyList() {
        // Given
        when(searchCriteriaRepository.findAll()).thenReturn(List.of());

        // When
        List<SearchCriteria> result = jobService.findAllSearchCriteria();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(searchCriteriaRepository).findAll();
    }

    @Test
    void saveSearchCriteria_WithValidCriteria_ShouldSaveAndReturnCriteria() {
        // Given
        SearchCriteria newCriteria = SearchCriteria.builder()
                .keywords(Collections.singleton("Spring Boot Developer"))
                .locations(Collections.singleton("London"))
                .build();

        SearchCriteria savedCriteria = SearchCriteria.builder()
                .id(3L)
                .keywords(Collections.singleton("Spring Boot Developer"))
                .locations(Collections.singleton("London"))
                .build();

        when(searchCriteriaRepository.save(newCriteria)).thenReturn(savedCriteria);

        // When
        SearchCriteria result = jobService.saveSearchCriteria(newCriteria);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getKeywords()).isEqualTo(Collections.singleton("Spring Boot Developer"));
        assertThat(result.getLocations()).isEqualTo(Collections.singleton("London"));
        verify(searchCriteriaRepository).save(newCriteria);
    }

    @Test
    void saveSearschCriteria_WithExistingCriteria_ShouldUpdateAndReturnCriteria() {
        // Given
        SearchCriteria existingCriteria = SearchCriteria.builder()
                .id(1L)
                .keywords(Collections.singleton("Java Developer"))
                .locations(Collections.singleton("Remote"))
                .build();

        SearchCriteria updatedCriteria = SearchCriteria.builder()
                .id(1L)
                .keywords(Collections.singleton("Senior Java Developer"))
                .locations(Collections.singleton("Remote"))
                .build();

        when(searchCriteriaRepository.save(existingCriteria)).thenReturn(updatedCriteria);

        // When
        SearchCriteria result = jobService.saveSearchCriteria(existingCriteria);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getKeywords()).isEqualTo(Collections.singleton("Senior Java Developer"));
        verify(searchCriteriaRepository).save(existingCriteria);
    }

    @Test
    void saveSearchCriteria_WithMinimalData_ShouldSaveSuccessfully() {
        // Given
        SearchCriteria minimalCriteria = SearchCriteria.builder()
                .keywords(Collections.singleton("Developer"))
                .build();

        SearchCriteria savedCriteria = SearchCriteria.builder()
                .id(4L)
                .keywords(Collections.singleton("Developer"))
                .build();

        when(searchCriteriaRepository.save(minimalCriteria)).thenReturn(savedCriteria);

        // When
        SearchCriteria result = jobService.saveSearchCriteria(minimalCriteria);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getKeywords()).isEqualTo(Collections.singleton("Developer"));
        verify(searchCriteriaRepository).save(minimalCriteria);
    }

    @Test
    void triggerJobScraping_ShouldCallJobScrapingService() {
        // Given
        List<Job> scrapedJobs = Arrays.asList(job1, job2);
        when(jobScrapingService.scrapAllPlatforms()).thenReturn(scrapedJobs);

        // When
        jobService.triggerJobScraping();

        // Then
        verify(jobScrapingService).scrapAllPlatforms();
        verify(jobScrapingService, times(1)).scrapAllPlatforms();
    }

    @Test
    void triggerJobScraping_WhenNoJobsScraped_ShouldStillCallService() {
        // Given
        when(jobScrapingService.scrapAllPlatforms()).thenReturn(List.of());

        // When
        jobService.triggerJobScraping();

        // Then
        verify(jobScrapingService).scrapAllPlatforms();
    }

    @Test
    void triggerJobScraping_WhenScrapingThrowsException_ShouldPropagateException() {
        // Given
        when(jobScrapingService.scrapAllPlatforms())
                .thenThrow(new RuntimeException("Scraping failed"));

        // When & Then
        try {
            jobService.triggerJobScraping();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Scraping failed");
        }

        verify(jobScrapingService).scrapAllPlatforms();
    }

    @Test
    void triggerJobScraping_ShouldNotInteractWithRepositories() {
        // Given
        when(jobScrapingService.scrapAllPlatforms()).thenReturn(List.of(job1));

        // When
        jobService.triggerJobScraping();

        // Then
        verify(jobScrapingService).scrapAllPlatforms();
        verifyNoInteractions(jobRepository, searchCriteriaRepository);
    }

    private Job createJob(Long id, String externalId, String title, String company) {
        Job job = new Job();
        job.setId(id);
        job.setExternalId(externalId);
        job.setTitle(title);
        job.setCompany(company);
        return job;
    }
}
