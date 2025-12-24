-- init.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    default_language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE source_files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id),
    file_name VARCHAR(255) NOT NULL,
    source_language VARCHAR(10) NOT NULL,
    format VARCHAR(20) NOT NULL,
    key_count INTEGER DEFAULT 0,
    file_path VARCHAR(500),
    uploaded_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE translations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_file_id UUID REFERENCES source_files(id),
    target_language VARCHAR(10) NOT NULL,
    key VARCHAR(255) NOT NULL,
    value TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    confidence DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(source_file_id, target_language, key)
);

CREATE TABLE generation_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_file_id UUID REFERENCES source_files(id),
    target_languages TEXT[] NOT NULL,
    output_formats JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_translations_source_file ON translations(source_file_id);
CREATE INDEX idx_translations_language ON translations(target_language);
