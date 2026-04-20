package com.wanted.projectmodule2lms.domain.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommentThreadDTO {
    private CommentViewDTO parent;
    private List<CommentViewDTO> replies;
}
