package com.vedisee.jobstracker.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record SearchCriteriaDto(
    @NotBlank(message = "Name is required")
    String name,
    Set<String> keywords,
    Set<String> locations
) {}
