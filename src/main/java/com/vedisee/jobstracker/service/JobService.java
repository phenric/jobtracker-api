package com.vedisee.jobstracker.service;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.SearchCriteria;

import java.util.List;
import java.util.Optional;

public interface JobService {
    List<Job> findAllJobs();
    Optional<Job> findJobById(Long id);
    List<SearchCriteria> findAllSearchCriteria();
    SearchCriteria saveSearchCriteria(SearchCriteria criteria);
    void triggerJobScraping();
}
