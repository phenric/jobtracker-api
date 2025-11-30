package com.vedisee.jobstracker.service;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.SearchCriteria;
import com.vedisee.jobstracker.repository.JobRepository;
import com.vedisee.jobstracker.repository.SearchCriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final SearchCriteriaRepository searchCriteriaRepository;
    private final JobScrapingService jobScrapingService;

    @Override
    public List<Job> findAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public Optional<Job> findJobById(Long id) {
        return jobRepository.findById(id);
    }

    @Override
    public List<SearchCriteria> findAllSearchCriteria() {
        return searchCriteriaRepository.findAll();
    }

    @Override
    @Transactional
    public SearchCriteria saveSearchCriteria(SearchCriteria criteria) {
        return searchCriteriaRepository.save(criteria);
    }

    @Override
    public void triggerJobScraping() {
        jobScrapingService.scrapAllPlatforms();
    }
}
