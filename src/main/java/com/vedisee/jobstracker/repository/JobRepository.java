package com.vedisee.jobstracker.repository;

import com.vedisee.jobstracker.model.Job;
import com.vedisee.jobstracker.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByExternalId(String externalId);

    List<Job> findByPlatform(Platform platform);

    @Query("SELECT j FROM Job j WHERE j.createdAt >= :since")
    List<Job> findNewJobsSince(@Param("since") LocalDateTime since);

    @Query("SELECT j FROM Job j WHERE " +
        "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> searchByKeyword(@Param("keyword") String keyword);
}
