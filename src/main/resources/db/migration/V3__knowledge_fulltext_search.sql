ALTER TABLE knowledge_chunk
    ADD COLUMN content_tsv tsvector
        GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;

CREATE INDEX idx_knowledge_chunk_tsv
    ON knowledge_chunk USING gin (content_tsv);