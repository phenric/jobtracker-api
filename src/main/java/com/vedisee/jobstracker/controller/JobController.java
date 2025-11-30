package com.vedisee.jobstracker.controller;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vedisee.jobstracker.dto.SearchCriteriaDto;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;

    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.findAllJobs());
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobService.findJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/criteria")
    public ResponseEntity<List<SearchCriteria>> getAllSearchCriteria() {
        return ResponseEntity.ok(jobService.findAllSearchCriteria());
    }

    @PostMapping("/criteria")
    public ResponseEntity<SearchCriteria> addSearchCriteria(@RequestBody @Valid SearchCriteriaDto dto) {
        SearchCriteria criteria = SearchCriteria.builder()
                .name(dto.name())
                .keywords(dto.keywords())
                .locations(dto.locations())
                .build();
        return ResponseEntity.ok(jobService.saveSearchCriteria(criteria));
    }

    @PostMapping("/jobs/scrape")
    public ResponseEntity<Void> triggerJobScraping() {
        jobService.triggerJobScraping();
        return ResponseEntity.ok().build();
    }
}
