package com.wanted.projectmodule2lms.domain.comment.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentDTO {

    private Integer commentId;
    private Integer postId;
    private Integer memberId;
    private Integer parentCommentId;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String memberName;
}
