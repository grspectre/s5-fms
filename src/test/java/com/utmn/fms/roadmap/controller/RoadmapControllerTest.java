package com.utmn.fms.roadmap.controller;

import com.utmn.fms.roadmap.dto.RecommendationDTO;
import com.utmn.fms.roadmap.dto.RoadmapDTO;
import com.utmn.fms.roadmap.dto.SurveyDTO;
import com.utmn.fms.roadmap.service.ExportService;
import com.utmn.fms.roadmap.service.RoadmapService;
import com.utmn.fms.roadmap.service.SurveyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoadmapController.class)
class RoadmapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoadmapService roadmapService;

    @MockitoBean
    private SurveyService surveyService;

    @MockitoBean
    private ExportService exportService;

    @Test
    @DisplayName("GET /api/roadmap/generate — валидная анкета найдена, путеводитель сформирован")
    void requestRoadmap_success() throws Exception {
        SurveyDTO surveyDTO = SurveyDTO.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .citizenship("Тестландия")
                .entryDate(LocalDate.now())
                .purposeOfStay("работа")
                .durationOfStay(90)
                .hasFingerprints(false)
                .hasMedicalExam(false)
                .isValid(true)
                .isDraft(false)
                .build();

        RoadmapDTO roadmapDTO = RoadmapDTO.builder()
                .id(5L)
                .createdDate(LocalDate.now())
                .recommendations(List.of(
                        RecommendationDTO.builder()
                                .id(100L)
                                .title("Миграционный учет")
                                .description("Описание")
                                .executionDate(LocalDate.now().plusDays(7))
                                .displayOrder(1)
                                .build()
                ))
                .build();

        Mockito.when(surveyService.findLastValidSurvey())
                .thenReturn(Optional.of(surveyDTO));

        Mockito.when(roadmapService.buildRoadmap(surveyDTO))
                .thenReturn(roadmapDTO);

        mockMvc.perform(get("/api/roadmap/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Путеводитель успешно сформирован"))
                .andExpect(jsonPath("$.data.id").value(5L))
                .andExpect(jsonPath("$.data.recommendations", hasSize(1)))
                .andExpect(jsonPath("$.data.recommendations[0].title").value("Миграционный учет"));
    }

    @Test
    @DisplayName("GET /api/roadmap/generate — валидная анкета не найдена, 400 с сообщением")
    void requestRoadmap_noValidSurvey_shouldReturn400() throws Exception {
        Mockito.when(surveyService.findLastValidSurvey())
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roadmap/generate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Не найдена валидная анкета. Сначала заполните и сохраните анкету."));
    }

    @Test
    @DisplayName("GET /api/roadmap/generate — ошибка сервиса, 500")
    void requestRoadmap_serviceError_shouldReturn500() throws Exception {
        SurveyDTO surveyDTO = SurveyDTO.builder()
                .id(1L)
                .build();

        Mockito.when(surveyService.findLastValidSurvey())
                .thenReturn(Optional.of(surveyDTO));

        Mockito.when(roadmapService.buildRoadmap(surveyDTO))
                .thenThrow(new RuntimeException("Generation error"));

        mockMvc.perform(get("/api/roadmap/generate"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка при формировании путеводителя"));
    }

    @Test
    @DisplayName("GET /api/roadmap/export — успешный экспорт, возвращается HTML‑файл")
    void exportRoadmap_success() throws Exception {
        RoadmapDTO roadmapDTO = RoadmapDTO.builder()
                .id(5L)
                .createdDate(LocalDate.now())
                .recommendations(List.of())
                .build();

        Mockito.when(roadmapService.findCurrentRoadmap())
                .thenReturn(Optional.of(roadmapDTO));

        String html = "<html><body>test</body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

        Mockito.when(exportService.exportToHtml(roadmapDTO))
                .thenReturn(bytes);

        mockMvc.perform(get("/api/roadmap/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=roadmap.html")))
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @DisplayName("GET /api/roadmap/export — путеводитель не найден, 400")
    void exportRoadmap_noRoadmap_shouldReturn400() throws Exception {
        Mockito.when(roadmapService.findCurrentRoadmap())
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roadmap/export"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Путеводитель не найден. Сначала создайте путеводитель."));
    }

    @Test
    @DisplayName("GET /api/roadmap/export — ошибка экспорта, 500")
    void exportRoadmap_error_shouldReturn500() throws Exception {
        RoadmapDTO roadmapDTO = RoadmapDTO.builder()
                .id(5L)
                .createdDate(LocalDate.now())
                .recommendations(List.of())
                .build();

        Mockito.when(roadmapService.findCurrentRoadmap())
                .thenReturn(Optional.of(roadmapDTO));

        Mockito.when(exportService.exportToHtml(roadmapDTO))
                .thenThrow(new RuntimeException("Export error"));

        mockMvc.perform(get("/api/roadmap/export"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка при экспорте путеводителя"));
    }
}