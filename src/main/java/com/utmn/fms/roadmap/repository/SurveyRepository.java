package com.utmn.fms.roadmap.repository;

import com.utmn.fms.roadmap.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("SELECT s FROM Survey s WHERE s.isDraft = true ORDER BY s.updatedAt DESC LIMIT 1")
    Optional<Survey> findLastDraft();

    @Query("SELECT s FROM Survey s WHERE s.isValid = true ORDER BY s.createdAt DESC LIMIT 1")
    Optional<Survey> findLastValidSurvey();
}