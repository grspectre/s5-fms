package com.utmn.fms.roadmap.service;

import com.utmn.fms.roadmap.dto.RecommendationDTO;
import com.utmn.fms.roadmap.dto.RoadmapDTO;
import com.utmn.fms.roadmap.dto.SurveyDTO;
import com.utmn.fms.roadmap.entity.Recommendation;
import com.utmn.fms.roadmap.entity.Roadmap;
import com.utmn.fms.roadmap.entity.Survey;
import com.utmn.fms.roadmap.repository.RoadmapRepository;
import com.utmn.fms.roadmap.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final SurveyRepository surveyRepository;

    @Transactional
    public RoadmapDTO buildRoadmap(SurveyDTO surveyDTO) {
        log.info("Building roadmap for survey id: {}", surveyDTO.getId());

        Survey survey = surveyRepository.findById(surveyDTO.getId())
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Roadmap roadmap = new Roadmap();
        roadmap.setSurvey(survey);
        roadmap.setCreatedDate(LocalDate.now());

        List<Recommendation> recommendations = generateRecommendations(survey, roadmap);
        roadmap.setRecommendations(recommendations);

        Roadmap saved = roadmapRepository.save(roadmap);
        return convertToDTO(saved);
    }

    private List<Recommendation> generateRecommendations(Survey survey, Roadmap roadmap) {
        List<Recommendation> recommendations = new ArrayList<>();
        int order = 1;

        LocalDate entryDate = survey.getEntryDate();

        // Миграционный учет (в течение 7 дней после въезда)
        recommendations.add(Recommendation.builder()
                .roadmap(roadmap)
                .title("Миграционный учет")
                .description("Необходимо встать на миграционный учет в течение 7 рабочих дней с момента въезда в Российскую Федерацию. Обратитесь в территориальное подразделение МВД России или в многофункциональный центр (МФЦ).")
                .executionDate(entryDate.plusDays(7))
                .displayOrder(order++)
                .build());

        // Дактилоскопия
        if (!survey.getHasFingerprints()) {
            recommendations.add(Recommendation.builder()
                    .roadmap(roadmap)
                    .title("Прохождение дактилоскопии")
                    .description("Необходимо пройти процедуру дактилоскопической регистрации в территориальном органе МВД России. Запишитесь на прием заранее через официальный сайт или по телефону.")
                    .executionDate(entryDate.plusDays(14))
                    .displayOrder(order++)
                    .build());
        }

        // Медосмотр
        if (!survey.getHasMedicalExam()) {
            recommendations.add(Recommendation.builder()
                    .roadmap(roadmap)
                    .title("Медицинское освидетельствование")
                    .description("Пройдите медицинское освидетельствование в медицинской организации, имеющей соответствующую лицензию. Получите сертификат об отсутствии ВИЧ-инфекции, сертификат об отсутствии инфекционных заболеваний и сертификат об отсутствии наркозависимости.")
                    .executionDate(entryDate.plusDays(21))
                    .displayOrder(order++)
                    .build());
        }

        // Разрешение на работу или патент (если цель - работа)
        if ("работа".equalsIgnoreCase(survey.getPurposeOfStay()) ||
                "трудоустройство".equalsIgnoreCase(survey.getPurposeOfStay())) {
            recommendations.add(Recommendation.builder()
                    .roadmap(roadmap)
                    .title("Получение патента на работу")
                    .description("Для осуществления трудовой деятельности необходимо получить патент. Обратитесь в территориальное подразделение МВД России с необходимыми документами (паспорт, миграционная карта, фотографии, медицинские сертификаты, документ об оплате патента).")
                    .executionDate(entryDate.plusDays(30))
                    .displayOrder(order++)
                    .build());
        }

        // Продление срока пребывания (если срок больше 90 дней)
        if (survey.getDurationOfStay() > 90) {
            recommendations.add(Recommendation.builder()
                    .roadmap(roadmap)
                    .title("Продление срока временного пребывания")
                    .description("Если планируемый срок пребывания превышает 90 дней, необходимо подать заявление о продлении срока временного пребывания в территориальное подразделение МВД России.")
                    .executionDate(entryDate.plusDays(60))
                    .displayOrder(order++)
                    .build());
        }

        // Напоминание о выезде
        recommendations.add(Recommendation.builder()
                .roadmap(roadmap)
                .title("Выезд из Российской Федерации")
                .description(String.format("Срок вашего пребывания истекает. Убедитесь, что вы покинете территорию Российской Федерации до %s или продлите документы на пребывание.",
                        entryDate.plusDays(survey.getDurationOfStay()).toString()))
                .executionDate(entryDate.plusDays(survey.getDurationOfStay() - 7))
                .displayOrder(order++)
                .build());

        return recommendations;
    }

    public Optional<RoadmapDTO> findCurrentRoadmap() {
        log.info("Finding current roadmap");
        return roadmapRepository.findCurrentRoadmap()
                .map(this::convertToDTO);
    }

    private RoadmapDTO convertToDTO(Roadmap roadmap) {
        List<RecommendationDTO> recommendationDTOs = roadmap.getRecommendations().stream()
                .map(rec -> RecommendationDTO.builder()
                        .id(rec.getId())
                        .title(rec.getTitle())
                        .description(rec.getDescription())
                        .executionDate(rec.getExecutionDate())
                        .displayOrder(rec.getDisplayOrder())
                        .build())
                .toList();

        return RoadmapDTO.builder()
                .id(roadmap.getId())
                .createdDate(roadmap.getCreatedDate())
                .recommendations(recommendationDTOs)
                .build();
    }
}