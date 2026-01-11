package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vector_store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorStore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vector_embedding_id")
    private UUID vectorEmbeddingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessons_id", nullable = false)
    private Lesson lesson;

    @Column(name = "vector_context_chunk", columnDefinition = "TEXT")
    private String contextChunk;

    @Column(name = "vector_embedding", columnDefinition = "vector(384)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private List<Double> embedding;
}