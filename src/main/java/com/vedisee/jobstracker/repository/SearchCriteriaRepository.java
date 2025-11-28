package com.vedisee.jobstracker.repository;

import com.vedisee.jobstracker.model.SearchCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchCriteriaRepository extends JpaRepository<SearchCriteria, Long> {
}
