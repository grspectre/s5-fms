package com.utmn.fms.roadmap.service;

import com.utmn.fms.roadmap.dto.RoadmapDTO;
import com.utmn.fms.roadmap.dto.RecommendationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    public byte[] exportToHtml(RoadmapDTO roadmap) {
        log.info("Exporting roadmap to HTML");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ru\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Путеводитель мигранта</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 40px; }\n");
        html.append("        h1 { color: #333; }\n");
        html.append("        .recommendation { margin-bottom: 30px; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }\n");
        html.append("        .recommendation h2 { color: #0066cc; margin-top: 0; }\n");
        html.append("        .date { color: #666; font-weight: bold; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>Путеводитель мигранта</h1>\n");
        html.append("    <p>Дата создания: ").append(roadmap.getCreatedDate()).append("</p>\n");
        html.append("    <hr>\n");

        for (RecommendationDTO rec : roadmap.getRecommendations()) {
            html.append("    <div class=\"recommendation\">\n");
            html.append("        <h2>").append(rec.getTitle()).append("</h2>\n");
            html.append("        <p class=\"date\">Дата выполнения: ").append(rec.getExecutionDate()).append("</p>\n");
            html.append("        <p>").append(rec.getDescription()).append("</p>\n");
            html.append("    </div>\n");
        }

        html.append("</body>\n");
        html.append("</html>");

        return html.toString().getBytes(StandardCharsets.UTF_8);
    }
}