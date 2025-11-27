package com.vedisee.jobstracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "search_criteria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "search_keywords", joinColumns = @JoinColumn(name = "criteria_id"))
    @Column(name = "keyword")
    private Set<String> keywords = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "search_locations", joinColumns = @JoinColumn(name = "criteria_id"))
    @Column(name = "location")
    private Set<String> locations = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
