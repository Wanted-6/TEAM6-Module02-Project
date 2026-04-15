package com.wanted.projectmodule2lms.domain.calendar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {

    private String id;
    private String title;
    private String start;
    private String color;
}