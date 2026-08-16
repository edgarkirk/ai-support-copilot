package com.epam.aisupportcopilot.repository;

import com.epam.aisupportcopilot.entity.KnowledgeChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    @Modifying
    @Query(value = "UPDATE knowledge_chunk SET embedding = cast(:embedding AS vector) WHERE id = :id",
            nativeQuery = true)
    void updateEmbedding(Long id, String embedding);

    @Query(value = """
            WITH vector_ranked AS (
                SELECT id, source, content,
                       ROW_NUMBER() OVER (ORDER BY embedding <=> cast(:queryEmbedding AS vector)) AS vector_rank
                FROM knowledge_chunk
                WHERE embedding IS NOT NULL
            ),
            keyword_ranked AS (
                SELECT id,
                       ROW_NUMBER() OVER (ORDER BY ts_rank(content_tsv, websearch_to_tsquery('english', :query)) DESC) AS keyword_rank
                FROM knowledge_chunk
                WHERE content_tsv @@ websearch_to_tsquery('english', :query)
            )
            SELECT v.id, v.source, v.content
            FROM vector_ranked v
            LEFT JOIN keyword_ranked k ON v.id = k.id
            ORDER BY (1.0 / (60 + v.vector_rank)) + COALESCE(1.0 / (60 + k.keyword_rank), 0) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeChunk> findHybrid(String queryEmbedding, String query, int limit);

    void deleteAllBySource(String source);
}