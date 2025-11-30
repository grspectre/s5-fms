package com.utmn.fms.roadmap.repository;

import com.utmn.fms.roadmap.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    @Query("SELECT r FROM Roadmap r LEFT JOIN FETCH r.recommendations WHERE r.id = (SELECT MAX(r2.id) FROM Roadmap r2)")
    Optional<Roadmap> findCurrentRoadmap();

    Optional<Roadmap> findBySurveyId(Long surveyId);
}