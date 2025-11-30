package com.utmn.fms.roadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDate executionDate;
    private Integer displayOrder;
}