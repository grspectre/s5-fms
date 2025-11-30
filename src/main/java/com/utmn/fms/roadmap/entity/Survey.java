package com.utmn.fms.roadmap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "surveys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "citizenship")
    private String citizenship;

    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "purpose_of_stay")
    private String purposeOfStay;

    @Column(name = "duration_of_stay")
    private Integer durationOfStay;

    @Column(name = "has_fingerprints")
    private Boolean hasFingerprints;

    @Column(name = "has_medical_exam")
    private Boolean hasMedicalExam;

    @Column(name = "is_draft")
    private Boolean isDraft;

    @Column(name = "is_valid")
    private Boolean isValid;

    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
        if (isDraft == null) {
            isDraft = true;
        }
        if (isValid == null) {
            isValid = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}