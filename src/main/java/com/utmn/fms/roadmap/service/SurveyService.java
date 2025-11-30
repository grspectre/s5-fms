package com.utmn.fms.roadmap.service;

import com.utmn.fms.roadmap.dto.SurveyDTO;
import com.utmn.fms.roadmap.entity.Survey;
import com.utmn.fms.roadmap.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public Optional<SurveyDTO> findLastDraft() {
        log.info("Finding last draft survey");
        return surveyRepository.findLastDraft()
                .map(this::convertToDTO);
    }

    @Transactional
    public SurveyDTO saveDraft(SurveyDTO surveyDTO) {
        log.info("Saving draft survey");
        Survey survey = convertToEntity(surveyDTO);
        survey.setIsDraft(true);
        survey.setIsValid(false);
        Survey saved = surveyRepository.save(survey);
        return convertToDTO(saved);
    }

    @Transactional
    public SurveyDTO createNewVersion(Long surveyId) {
        log.info("Creating new version of survey with id: {}", surveyId);
        Survey existingSurvey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Survey newVersion = Survey.builder()
                .fullName(existingSurvey.getFullName())
                .citizenship(existingSurvey.getCitizenship())
                .entryDate(existingSurvey.getEntryDate())
                .purposeOfStay(existingSurvey.getPurposeOfStay())
                .durationOfStay(existingSurvey.getDurationOfStay())
                .hasFingerprints(existingSurvey.getHasFingerprints())
                .hasMedicalExam(existingSurvey.getHasMedicalExam())
                .isDraft(false)
                .isValid(true)
                .version(existingSurvey.getVersion() + 1)
                .build();

        Survey saved = surveyRepository.save(newVersion);
        return convertToDTO(saved);
    }

    public Optional<SurveyDTO> findLastValidSurvey() {
        log.info("Finding last valid survey");
        return surveyRepository.findLastValidSurvey()
                .map(this::convertToDTO);
    }

    private SurveyDTO convertToDTO(Survey survey) {
        return SurveyDTO.builder()
                .id(survey.getId())
                .fullName(survey.getFullName())
                .citizenship(survey.getCitizenship())
                .entryDate(survey.getEntryDate())
                .purposeOfStay(survey.getPurposeOfStay())
                .durationOfStay(survey.getDurationOfStay())
                .hasFingerprints(survey.getHasFingerprints())
                .hasMedicalExam(survey.getHasMedicalExam())
                .isDraft(survey.getIsDraft())
                .isValid(survey.getIsValid())
                .version(survey.getVersion())
                .build();
    }

    private Survey convertToEntity(SurveyDTO dto) {
        return Survey.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .citizenship(dto.getCitizenship())
                .entryDate(dto.getEntryDate())
                .purposeOfStay(dto.getPurposeOfStay())
                .durationOfStay(dto.getDurationOfStay())
                .hasFingerprints(dto.getHasFingerprints())
                .hasMedicalExam(dto.getHasMedicalExam())
                .isDraft(dto.getIsDraft())
                .isValid(dto.getIsValid())
                .version(dto.getVersion())
                .build();
    }
}