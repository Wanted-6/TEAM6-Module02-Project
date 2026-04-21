package com.wanted.projectmodule2lms.domain.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
public class CommentViewDTO {
    private Integer commentId;
    private Integer postId;
    private Integer memberId;
    private Integer parentCommentId;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String memberName;
    private String profileImage;
}
