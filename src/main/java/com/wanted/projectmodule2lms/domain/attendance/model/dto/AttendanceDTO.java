package com.wanted.projectmodule2lms.domain.attendance.model.dto;

import com.wanted.projectmodule2lms.domain.attendance.model.entity.AttendanceStatus;
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
public class AttendanceDTO {

    private Integer attendanceId;
    private Integer enrollmentId;
    private Integer sectionId;
    private AttendanceStatus status;
    private LocalDateTime checkedAt;
    private LocalDateTime recordedAt;
    private Boolean isManual;
    private String note;
}
