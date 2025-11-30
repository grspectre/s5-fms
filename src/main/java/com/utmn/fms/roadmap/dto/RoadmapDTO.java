package com.utmn.fms.roadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapDTO {

    private Long id;
    private LocalDate createdDate;
    private List<RecommendationDTO> recommendations;
}