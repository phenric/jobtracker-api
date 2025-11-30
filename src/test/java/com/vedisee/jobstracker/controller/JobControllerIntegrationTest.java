package com.vedisee.jobstracker.controller;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.Platform;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.repository.JobRepository;
import com.vedisee.jobstracker.repository.SearchCriteriaRepository;
import com.vedisee.jobstracker.service.JobScrapingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SearchCriteriaRepository searchCriteriaRepository;

    @MockitoBean
    private JobScrapingService jobScrapingService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        searchCriteriaRepository.deleteAll();
    }

    @Test
    void shouldGetAllJobs() throws Exception {
        Job job = Job.builder()
                .externalId("123")
                .title("Java Developer")
                .company("Tech Corp")
                .description("Great job")
                .platform(Platform.LINKEDIN)
                .location("Remote")
                .url("http://example.com")
                .build();
        jobRepository.save(job);

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }

    @Test
    void shouldGetJobById() throws Exception {
        Job job = Job.builder()
                .externalId("123")
                .title("Java Developer")
                .company("Tech Corp")
                .description("Great job")
                .platform(Platform.LINKEDIN)
                .location("Remote")
                .url("http://example.com")
                .build();
        Job savedJob = jobRepository.save(job);

        mockMvc.perform(get("/api/jobs/{id}", savedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void shouldReturn404WhenJobNotFound() throws Exception {
        mockMvc.perform(get("/api/jobs/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllSearchCriteria() throws Exception {
        SearchCriteria criteria = SearchCriteria.builder()
                .name("Java Jobs")
                .keywords(Set.of("Java", "Spring"))
                .locations(Set.of("Remote"))
                .build();
        searchCriteriaRepository.save(criteria);

        mockMvc.perform(get("/api/criteria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Jobs"));
    }

    @Test
    void shouldAddSearchCriteria() throws Exception {
        SearchCriteria criteria = SearchCriteria.builder()
                .name("Python Jobs")
                .keywords(Set.of("Python", "Django"))
                .locations(Set.of("Berlin"))
                .build();

        mockMvc.perform(post("/api/criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Python Jobs"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldTriggerJobScraping() throws Exception {
        mockMvc.perform(post("/api/jobs/scrape"))
                .andExpect(status().isOk());

        verify(jobScrapingService).scrapAllPlatforms();
    }
}
