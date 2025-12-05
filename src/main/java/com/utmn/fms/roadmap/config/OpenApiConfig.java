package com.utmn.fms.roadmap.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Путеводитель мигранта – API",
                version = "1.0",
                description = "REST API для анкеты и формирования путеводителя мигранта"
        )
)
public class OpenApiConfig {}