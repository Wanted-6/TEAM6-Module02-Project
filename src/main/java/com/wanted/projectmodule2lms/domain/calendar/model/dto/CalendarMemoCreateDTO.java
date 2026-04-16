package com.wanted.projectmodule2lms.domain.calendar.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CalendarMemoCreateDTO {

    private String content;
    private String memoDate;
}