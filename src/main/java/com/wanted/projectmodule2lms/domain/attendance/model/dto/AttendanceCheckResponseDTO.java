package com.wanted.projectmodule2lms.domain.attendance.model.dto;

import com.wanted.projectmodule2lms.domain.attendance.model.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceCheckResponseDTO {

    private AttendanceStatus status;
    private String message;
}
