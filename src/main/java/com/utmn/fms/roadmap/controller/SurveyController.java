package com.utmn.fms.roadmap.controller;

import com.utmn.fms.roadmap.dto.ApiResponse;
import com.utmn.fms.roadmap.dto.SurveyDTO;
import com.utmn.fms.roadmap.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/open")
    public ResponseEntity<ApiResponse<SurveyDTO>> openForm() {
        log.info("Opening survey form");

        Optional<SurveyDTO> draft = surveyService.findLastDraft();

        if (draft.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Найден черновик анкеты", draft.get()));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Новая анкета", null));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SurveyDTO>> submitAnswers(
            @Valid @RequestBody SurveyDTO surveyDTO,
            BindingResult bindingResult) {

        log.info("Submitting survey answers");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибки валидации", errors));
        }

        try {
            SurveyDTO saved = surveyService.saveDraft(surveyDTO);
            return ResponseEntity.ok(ApiResponse.success("Анкета успешно сохранена как черновик", saved));
        } catch (Exception e) {
            log.error("Error saving survey draft", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Ошибка при сохранении анкеты"));
        }
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<ApiResponse<SurveyDTO>> confirmSave(@PathVariable Long id) {
        log.info("Confirming survey save for id: {}", id);

        try {
            SurveyDTO saved = surveyService.createNewVersion(id);
            return ResponseEntity.ok(ApiResponse.success("Анкета успешно сохранена", saved));
        } catch (RuntimeException e) {
            log.error("Error confirming survey save", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error confirming survey save", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Ошибка при сохранении анкеты"));
        }
    }
}