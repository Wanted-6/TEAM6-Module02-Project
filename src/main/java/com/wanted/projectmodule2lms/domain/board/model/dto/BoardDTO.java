package com.wanted.projectmodule2lms.domain.board.model.dto;

import com.wanted.projectmodule2lms.domain.board.model.entity.AnswerStatus;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BoardDTO {

    private Integer postId;
    private Integer memberId;
    private String memberName;
    private Integer courseId;
    private String courseTitle;
    private Integer courseInstructorId;
    private Integer sectionId;
    private String sectionTitle;
    private String title;
    private String content;
    private BoardType postType;
    private Boolean isSecret;
    private AnswerStatus answerStatus;
    private Integer viewCount;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String profileImage;
}
