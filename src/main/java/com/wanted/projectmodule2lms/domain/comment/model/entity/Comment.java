package com.wanted.projectmodule2lms.domain.comment.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Comment")
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;

    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "parent_comment_id")
    private Integer parentCommentId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Comment(Integer postId, Integer memberId, Integer parentCommentId, String content) {
        this.postId = postId;
        this.memberId = memberId;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.isDeleted = false;
    }
    public void changeContent(String content) {


        this.content = content;
    }
    public void deleteComment() {
        this.isDeleted = true;
    }


}
