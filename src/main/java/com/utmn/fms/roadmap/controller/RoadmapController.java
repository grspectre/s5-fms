package com.utmn.fms.roadmap.controller;

import com.utmn.fms.roadmap.dto.ApiResponse;
import com.utmn.fms.roadmap.dto.RoadmapDTO;
import com.utmn.fms.roadmap.dto.SurveyDTO;
import com.utmn.fms.roadmap.service.ExportService;
import com.utmn.fms.roadmap.service.RoadmapService;
import com.utmn.fms.roadmap.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final SurveyService surveyService;
    private final ExportService exportService;

    @GetMapping("/generate")
    public ResponseEntity<ApiResponse<RoadmapDTO>> requestRoadmap() {
        log.info("Requesting roadmap generation");

        Optional<SurveyDTO> validSurvey = surveyService.findLastValidSurvey();

        if (validSurvey.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Не найдена валидная анкета. Сначала заполните и сохраните анкету."));
        }

        try {
            RoadmapDTO roadmap = roadmapService.buildRoadmap(validSurvey.get());
            return ResponseEntity.ok(ApiResponse.success("Путеводитель успешно сформирован", roadmap));
        } catch (Exception e) {
            log.error("Error generating roadmap", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Ошибка при формировании путеводителя"));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportRoadmap() {
        log.info("Exporting roadmap");

        Optional<RoadmapDTO> roadmap = roadmapService.findCurrentRoadmap();

        if (roadmap.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Путеводитель не найден. Сначала создайте путеводитель."));
        }

        try {
            byte[] content = exportService.exportToHtml(roadmap.get());
            ByteArrayResource resource = new ByteArrayResource(content);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roadmap.html")
                    .contentType(MediaType.TEXT_HTML)
                    .contentLength(content.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error exporting roadmap", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Ошибка при экспорте путеводителя"));
        }
    }
}