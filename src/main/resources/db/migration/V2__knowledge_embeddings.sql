CREATE TABLE knowledge_chunk
(
    id        BIGSERIAL PRIMARY KEY,
    source    VARCHAR(255) NOT NULL,
    content   TEXT         NOT NULL,
    embedding vector(1536)
);

CREATE INDEX idx_knowledge_chunk_embedding
    ON knowledge_chunk USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 10);