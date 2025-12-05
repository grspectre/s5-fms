package com.utmn.fms.roadmap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utmn.fms.roadmap.dto.SurveyDTO;
import com.utmn.fms.roadmap.service.SurveyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SurveyController.class)
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurveyService surveyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/survey/open — найден черновик, возвращается success и данные анкеты")
    void openForm_draftExists_shouldReturnDraft() throws Exception {
        SurveyDTO draft = SurveyDTO.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .citizenship("Тестландия")
                .entryDate(LocalDate.now())
                .purposeOfStay("работа")
                .durationOfStay(90)
                .hasFingerprints(true)
                .hasMedicalExam(false)
                .isDraft(true)
                .isValid(false)
                .version(1)
                .build();

        Mockito.when(surveyService.findLastDraft())
                .thenReturn(Optional.of(draft));

        mockMvc.perform(get("/api/survey/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Найден черновик анкеты"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.fullName").value("Иванов Иван Иванович"))
                .andExpect(jsonPath("$.data.citizenship").value("Тестландия"));
    }

    @Test
    @DisplayName("GET /api/survey/open — черновик не найден, новая анкета")
    void openForm_noDraft_shouldReturnNewSurvey() throws Exception {
        Mockito.when(surveyService.findLastDraft())
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/survey/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Новая анкета"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/survey/submit — валидный запрос, черновик сохраняется")
    void submitAnswers_validRequest_shouldSaveDraft() throws Exception {
        SurveyDTO requestDto = SurveyDTO.builder()
                .fullName("Иванов Иван Иванович")
                .citizenship("Тестландия")
                .entryDate(LocalDate.now())
                .purposeOfStay("работа")
                .durationOfStay(90)
                .hasFingerprints(true)
                .hasMedicalExam(true)
                .build();

        SurveyDTO savedDto = SurveyDTO.builder()
                .id(10L)
                .fullName(requestDto.getFullName())
                .citizenship(requestDto.getCitizenship())
                .entryDate(requestDto.getEntryDate())
                .purposeOfStay(requestDto.getPurposeOfStay())
                .durationOfStay(requestDto.getDurationOfStay())
                .hasFingerprints(requestDto.getHasFingerprints())
                .hasMedicalExam(requestDto.getHasMedicalExam())
                .isDraft(true)
                .isValid(false)
                .version(1)
                .build();

        Mockito.when(surveyService.saveDraft(any(SurveyDTO.class)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/survey/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Анкета успешно сохранена как черновик"))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.fullName").value("Иванов Иван Иванович"));
    }

    @Test
    @DisplayName("POST /api/survey/submit — невалидные данные, возвращаются ошибки валидации")
    void submitAnswers_invalidRequest_shouldReturnValidationErrors() throws Exception {
        // отправляем объект без обязательных полей: fullName, citizenship, entryDate, purposeOfStay, durationOfStay
        SurveyDTO invalidDto = SurveyDTO.builder()
                .hasFingerprints(true)
                .hasMedicalExam(false)
                .build();

        mockMvc.perform(post("/api/survey/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибки валидации"))
                .andExpect(jsonPath("$.errors.fullName", notNullValue()))
                .andExpect(jsonPath("$.errors.citizenship", notNullValue()))
                .andExpect(jsonPath("$.errors.entryDate", notNullValue()))
                .andExpect(jsonPath("$.errors.purposeOfStay", notNullValue()))
                .andExpect(jsonPath("$.errors.durationOfStay", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/survey/submit — ошибка сервиса, 500 и сообщение")
    void submitAnswers_serviceError_shouldReturn500() throws Exception {
        SurveyDTO requestDto = SurveyDTO.builder()
                .fullName("Иванов Иван Иванович")
                .citizenship("Тестландия")
                .entryDate(LocalDate.now())
                .purposeOfStay("работа")
                .durationOfStay(90)
                .hasFingerprints(true)
                .hasMedicalExam(true)
                .build();

        Mockito.when(surveyService.saveDraft(any(SurveyDTO.class)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/survey/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка при сохранении анкеты"));
    }

    @Test
    @DisplayName("POST /api/survey/confirm/{id} — успешное создание новой версии")
    void confirmSave_success() throws Exception {
        Long id = 1L;
        SurveyDTO newVersion = SurveyDTO.builder()
                .id(2L)
                .fullName("Иванов Иван Иванович")
                .citizenship("Тестландия")
                .entryDate(LocalDate.now())
                .purposeOfStay("работа")
                .durationOfStay(90)
                .hasFingerprints(true)
                .hasMedicalExam(true)
                .isDraft(false)
                .isValid(true)
                .version(2)
                .build();

        Mockito.when(surveyService.createNewVersion(id))
                .thenReturn(newVersion);

        mockMvc.perform(post("/api/survey/confirm/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Анкета успешно сохранена"))
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.version").value(2));
    }

    @Test
    @DisplayName("POST /api/survey/confirm/{id} — бизнес‑ошибка (RuntimeException), 400 с текстом из исключения")
    void confirmSave_runtimeError_shouldReturn400() throws Exception {
        Long id = 999L;
        Mockito.when(surveyService.createNewVersion(id))
                .thenThrow(new RuntimeException("Survey not found"));

        mockMvc.perform(post("/api/survey/confirm/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Survey not found"));
    }

    @Test
    @DisplayName("POST /api/survey/confirm/{id} — неожиданная ошибка, 400")
    void confirmSave_unexpectedError_shouldReturn400() throws Exception {
        Long id = 1L;
        Mockito.when(surveyService.createNewVersion(id))
                .thenThrow(new IllegalStateException("Unexpected"));

        mockMvc.perform(post("/api/survey/confirm/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unexpected"));
    }
}