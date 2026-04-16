package com.wanted.projectmodule2lms.domain.calendar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CalendarMemoDTO {

    private Integer memoId;
    private String content;
    private String memoDate;
}