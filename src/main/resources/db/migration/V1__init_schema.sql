CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE documents
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     VARCHAR(255) NOT NULL,
    title         VARCHAR(500) NOT NULL,
    category      VARCHAR(100),
    filename      VARCHAR(500) NOT NULL,
    content_hash  VARCHAR(64)  NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    error_message TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_documents PRIMARY KEY (id),
    CONSTRAINT uq_documents_tenant_content_hash UNIQUE (tenant_id, content_hash),
    CONSTRAINT chk_documents_status CHECK (status IN ('PROCESSING', 'READY', 'FAILED'))
);

CREATE TABLE document_chunks
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    document_id  UUID         NOT NULL,
    tenant_id    VARCHAR(255) NOT NULL,
    chunk_index  INTEGER      NOT NULL,
    content      TEXT         NOT NULL,
    page_number  INTEGER,
    token_count  INTEGER,
    embedding    VECTOR(1536),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_document_chunks PRIMARY KEY (id),
    CONSTRAINT fk_document_chunks_document
        FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT uq_document_chunks_document_index
        UNIQUE (document_id, chunk_index)
);

CREATE TABLE conversations
(
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(255) NOT NULL,
    title            VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at  TIMESTAMPTZ,

    CONSTRAINT pk_conversations PRIMARY KEY (id)
);

CREATE TABLE messages
(
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    conversation_id   UUID         NOT NULL,
    role              VARCHAR(30)  NOT NULL,
    content           TEXT         NOT NULL,
    token_count       INTEGER,
    model             VARCHAR(255),
    latency_ms        BIGINT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT chk_messages_role
        CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

CREATE TABLE message_sources
(
    id                UUID             NOT NULL DEFAULT gen_random_uuid(),
    message_id        UUID             NOT NULL,
    chunk_id          UUID             NOT NULL,
    similarity_score  DOUBLE PRECISION NOT NULL,

    CONSTRAINT pk_message_sources PRIMARY KEY (id),
    CONSTRAINT fk_message_sources_message
        FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sources_chunk
        FOREIGN KEY (chunk_id) REFERENCES document_chunks(id) ON DELETE CASCADE
);

CREATE INDEX idx_documents_tenant_category
    ON documents (tenant_id, category);

CREATE INDEX idx_document_chunks_tenant
    ON document_chunks (tenant_id);

CREATE INDEX idx_document_chunks_document
    ON document_chunks (document_id);

CREATE INDEX idx_conversations_tenant
    ON conversations (tenant_id);

CREATE INDEX idx_messages_conversation
    ON messages (conversation_id);

CREATE INDEX idx_message_sources_message
    ON message_sources (message_id);

CREATE INDEX idx_document_chunks_embedding_hnsw
    ON document_chunks USING hnsw (embedding vector_cosine_ops);