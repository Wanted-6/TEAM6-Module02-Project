package com.wanted.projectmodule2lms.domain.board.model.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Board(Builder builder) {
        this.memberId = builder.memberId;
        this.courseId = builder.courseId;
        this.sectionId = builder.sectionId;
        this.title = builder.title;
        this.content = builder.content;
        this.postType = builder.postType;
        this.isSecret = builder.isSecret;
        this.answerStatus = builder.answerStatus;
        this.viewCount = builder.viewCount;
        this.isDeleted = builder.isDeleted;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void modifyBoard(Integer courseId,
                            Integer sectionId,
                            String title,
                            String content,
                            BoardType postType,
                            Boolean isSecret,
                            AnswerStatus answerStatus) {
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.isSecret = isSecret;
        this.answerStatus = answerStatus;
    }

    public static class Builder {
        private Integer memberId;
        private Integer courseId;
        private Integer sectionId;
        private String title;
        private String content;
        private BoardType postType;
        private Boolean isSecret;
        private AnswerStatus answerStatus;
        private Integer viewCount;
        private Boolean isDeleted;

        public Builder memberId(Integer memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder courseId(Integer courseId) {
            this.courseId = courseId;
            return this;
        }

        public Builder sectionId(Integer sectionId) {
            this.sectionId = sectionId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder postType(BoardType postType) {
            this.postType = postType;
            return this;
        }

        public Builder isSecret(Boolean isSecret) {
            this.isSecret = isSecret;
            return this;
        }

        public Builder answerStatus(AnswerStatus answerStatus) {
            this.answerStatus = answerStatus;
            return this;
        }

        public Builder viewCount(Integer viewCount) {
            this.viewCount = viewCount;
            return this;
        }

        public Builder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Board build() {
            return new Board(this);
        }


    }
    public void deleteBoard() {
        this.isDeleted = true;
    }

    public void increasedViewCount() {
        this.viewCount++;
    }
}
