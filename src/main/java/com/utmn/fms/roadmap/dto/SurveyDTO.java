package com.utmn.fms.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyDTO {

    private Long id;

    @NotBlank(message = "ФИО обязательно для заполнения")
    private String fullName;

    @NotBlank(message = "Гражданство обязательно для заполнения")
    private String citizenship;

    @NotNull(message = "Дата въезда обязательна для заполнения")
    @PastOrPresent(message = "Дата въезда не может быть в будущем")
    private LocalDate entryDate;

    @NotBlank(message = "Цель пребывания обязательна для заполнения")
    private String purposeOfStay;

    @NotNull(message = "Срок пребывания обязателен для заполнения")
    @Positive(message = "Срок пребывания должен быть положительным числом")
    private Integer durationOfStay;

    @NotNull(message = "Необходимо указать наличие дактилоскопии")
    private Boolean hasFingerprints;

    @NotNull(message = "Необходимо указать наличие медосмотра")
    private Boolean hasMedicalExam;

    private Boolean isDraft;
    private Boolean isValid;
    private Integer version;
}