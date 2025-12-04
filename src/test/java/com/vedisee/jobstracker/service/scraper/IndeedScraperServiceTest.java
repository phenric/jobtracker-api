package com.vedisee.jobstracker.service.scraper;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.Platform;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.service.SiteAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndeedScraperServiceTest {

    @Mock
    private Environment environment;

    @Mock
    private SiteAvailabilityService siteAvailabilityService;

    private IndeedScraperService indeedScraperService;

    @BeforeEach
    void setUp() {
        indeedScraperService = new IndeedScraperService(environment, siteAvailabilityService);
    }

    @Test
    void getPlatform_ShouldReturnIndeed() {
        // When
        Platform platform = indeedScraperService.getPlatform();

        // Then
        assertThat(platform).isEqualTo(Platform.INDEED);
    }

    @Test
    void isAvailable_WhenSiteIsReachable_ShouldReturnTrue() {
        // Given
        when(siteAvailabilityService.isSiteReachable(anyString())).thenReturn(true);

        // When
        boolean result = indeedScraperService.isAvailable();

        // Then
        assertThat(result).isTrue();
        verify(siteAvailabilityService).isSiteReachable(Platform.INDEED.getUrl());
    }

    @Test
    void isAvailable_WhenSiteIsNotReachable_ShouldReturnFalse() {
        // Given
        when(siteAvailabilityService.isSiteReachable(anyString())).thenReturn(false);

        // When
        boolean result = indeedScraperService.isAvailable();

        // Then
        assertThat(result).isFalse();
        verify(siteAvailabilityService).isSiteReachable(Platform.INDEED.getUrl());
    }

    @Test
    void scrapeJobs_InDevProfile_WithSingleKeywordAndLocation_ShouldReturnThreeJobs() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Test Criteria")
                .keywords(Collections.singleton("Java Developer"))
                .locations(Collections.singleton("Paris"))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).hasSize(3);
        assertThat(jobs).allMatch(job -> job.getPlatform() == Platform.INDEED);
        assertThat(jobs).allMatch(job -> job.getTitle().contains("Java Developer"));
        assertThat(jobs).allMatch(job -> job.getDescription().contains("Paris"));
        assertThat(jobs).allMatch(job -> job.getExternalId().startsWith("Indeed-"));
        assertThat(jobs).allMatch(job -> job.getUrl().startsWith(Platform.INDEED.getUrl()));

        verify(environment, atLeastOnce()).acceptsProfiles(any(Profiles.class));
    }

    @Test
    void scrapeJobs_InDevProfile_WithMultipleKeywordsAndLocations_ShouldReturnCorrectNumberOfJobs() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Multi Criteria")
                .keywords(new HashSet<>(Arrays.asList("Java Developer", "Python Developer")))
                .locations(new HashSet<>(Arrays.asList("Paris", "Lyon")))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        // 2 keywords * 2 locations * 3 jobs per combination = 12 jobs
        assertThat(jobs).hasSize(12);
        assertThat(jobs).allMatch(job -> job.getPlatform() == Platform.INDEED);

        // Verify we have jobs for both keywords
        assertThat(jobs.stream().anyMatch(job -> job.getTitle().contains("Java Developer"))).isTrue();
        assertThat(jobs.stream().anyMatch(job -> job.getTitle().contains("Python Developer"))).isTrue();

        // Verify we have jobs for both locations
        assertThat(jobs.stream().anyMatch(job -> job.getDescription().contains("Paris"))).isTrue();
        assertThat(jobs.stream().anyMatch(job -> job.getDescription().contains("Lyon"))).isTrue();
    }

    @Test
    void scrapeJobs_NotInDevProfile_ShouldReturnEmptyList() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Test Criteria")
                .keywords(Collections.singleton("Java Developer"))
                .locations(Collections.singleton("Paris"))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).isEmpty();
        verify(environment, atLeastOnce()).acceptsProfiles(any(Profiles.class));
    }

    @Test
    void scrapeJobs_WithEmptyKeywords_ShouldReturnEmptyList() {
        // Given
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Empty Keywords")
                .keywords(Collections.emptySet())
                .locations(Collections.singleton("Paris"))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).isEmpty();
    }

    @Test
    void scrapeJobs_WithEmptyLocations_ShouldReturnEmptyList() {
        // Given
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Empty Locations")
                .keywords(Collections.singleton("Java Developer"))
                .locations(Collections.emptySet())
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).isEmpty();
    }

    @Test
    void scrapeJobs_InDevProfile_ShouldGenerateUniqueExternalIds() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Test Criteria")
                .keywords(Collections.singleton("Java Developer"))
                .locations(Collections.singleton("Paris"))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).hasSize(3);

        // Verify all external IDs are unique
        long uniqueExternalIds = jobs.stream()
                .map(Job::getExternalId)
                .distinct()
                .count();
        assertThat(uniqueExternalIds).isEqualTo(3);
    }

    @Test
    void scrapeJobs_InDevProfile_ShouldGenerateUniqueUrls() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Test Criteria")
                .keywords(Collections.singleton("Java Developer"))
                .locations(Collections.singleton("Paris"))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).hasSize(3);

        // Verify all URLs are unique
        long uniqueUrls = jobs.stream()
                .map(Job::getUrl)
                .distinct()
                .count();
        assertThat(uniqueUrls).isEqualTo(3);
    }

    @Test
    void scrapeJobs_InDevProfile_ShouldSetCorrectJobProperties() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        String keyword = "Senior Java Developer";
        String location = "Remote";

        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Test Criteria")
                .keywords(Collections.singleton(keyword))
                .locations(Collections.singleton(location))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).hasSize(3);

        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            assertThat(job.getTitle()).isEqualTo(keyword + " - Position " + (i + 1));
            assertThat(job.getCompany()).isEqualTo("Company " + (i + 1));
            assertThat(job.getDescription())
                    .contains(keyword)
                    .contains(location);
            assertThat(job.getPlatform()).isEqualTo(Platform.INDEED);
            assertThat(job.getExternalId()).startsWith("Indeed-");
            assertThat(job.getUrl()).startsWith(Platform.INDEED.getUrl());
        }
    }

    @Test
    void scrapeJobs_WithNullKeywords_ShouldHandleGracefully() {
        // Given
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Null Keywords")
                .keywords(null)
                .locations(Collections.singleton("Paris"))
                .build();

        // When & Then
        // Should not throw exception
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);
        assertThat(jobs).isEmpty();
    }

    @Test
    void scrapeJobs_WithNullLocations_ShouldHandleGracefully() {
        // Given
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Null Locations")
                .keywords(Collections.singleton("Java Developer"))
                .locations(null)
                .build();

        // When & Then
        // Should not throw exception
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);
        assertThat(jobs).isEmpty();
    }

    @Test
    void scrapeJobs_InDevProfile_WithMultipleKeywords_ShouldContainAllKeywordsInResults() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        // Use distinct keywords that won't overlap
        List<String> keywords = Arrays.asList("Data Scientist", "Product Manager", "UX Designer");
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Multiple Keywords")
                .keywords(new HashSet<>(keywords))
                .locations(Collections.singleton("Paris"))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).hasSize(9); // 3 keywords * 1 location * 3 jobs = 9

        // Verify each keyword appears exactly 3 times
        assertThat(jobs.stream().filter(job -> job.getTitle().contains("Data Scientist")).count()).isEqualTo(3);
        assertThat(jobs.stream().filter(job -> job.getTitle().contains("Product Manager")).count()).isEqualTo(3);
        assertThat(jobs.stream().filter(job -> job.getTitle().contains("UX Designer")).count()).isEqualTo(3);
    }

    @Test
    void scrapeJobs_InDevProfile_WithMultipleLocations_ShouldContainAllLocationsInResults() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        List<String> locations = Arrays.asList("Paris", "Lyon", "Marseille");
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .name("Multiple Locations")
                .keywords(Collections.singleton("Java Developer"))
                .locations(new HashSet<>(locations))
                .build();

        // When
        List<Job> jobs = indeedScraperService.scrapeJobs(searchCriteria);

        // Then
        assertThat(jobs).hasSize(9); // 1 keyword * 3 locations * 3 jobs = 9

        // Verify each location appears in the results
        for (String location : locations) {
            long countForLocation = jobs.stream()
                    .filter(job -> job.getDescription().contains(location))
                    .count();
            assertThat(countForLocation).isEqualTo(3);
        }
    }
}
