package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "modules_id")
    private UUID moduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "modules_title", length = 200)
    private String title;

    @Column(name = "modules_order_index")
    private Integer orderIndex;

    @Builder.Default
    @Column(name = "is_pending")
    private Boolean isPending = false;

    @Builder.Default
    @Column(name = "is_pending_deletion")
    private Boolean isPendingDeletion = false;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lesson> lessons;
}