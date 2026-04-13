package com.wanted.projectmodule2lms.domain.board.model.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Post")
@Getter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private BoardType postType;

    @Column(name = "is_secret", nullable = false)
    private Boolean isSecret;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_status")
    private AnswerStatus answerStatus;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
