-- schema.sql
-- Таблица анкет
CREATE TABLE IF NOT EXISTS surveys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    citizenship VARCHAR(100) NOT NULL,
    entry_date DATE NOT NULL,
    purpose_of_stay VARCHAR(255) NOT NULL,
    duration_of_stay INT NOT NULL,
    has_fingerprints BOOLEAN NOT NULL DEFAULT FALSE,
    has_medical_exam BOOLEAN NOT NULL DEFAULT FALSE,
    is_draft BOOLEAN NOT NULL DEFAULT TRUE,
    is_valid BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица путеводителей
CREATE TABLE IF NOT EXISTS roadmaps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    created_date DATE NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT fk_roadmap_survey FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
);

-- Таблица рекомендаций
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    execution_date DATE NOT NULL,
    display_order INT NOT NULL,
    CONSTRAINT fk_recommendation_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE
);

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_survey_draft ON surveys(is_draft, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_survey_valid ON surveys(is_valid, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_roadmap_survey ON roadmaps(survey_id);
CREATE INDEX IF NOT EXISTS idx_recommendation_roadmap ON recommendations(roadmap_id, display_order);