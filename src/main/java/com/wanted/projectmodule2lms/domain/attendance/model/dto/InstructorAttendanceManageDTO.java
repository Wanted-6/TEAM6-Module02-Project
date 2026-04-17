package com.wanted.projectmodule2lms.domain.attendance.model.dto;

import com.wanted.projectmodule2lms.domain.section.model.dto.SectionAttendanceDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class InstructorAttendanceManageDTO {

    private Integer memberId;
    private Integer enrollmentId;
    private String studentName;
    private String email;
    private Integer progressRate;
    private String attendanceSummary;
    private Double assignmentScore;
    private Double examScore;
    private Double attitudeScore;
    private String attitudeMemo;
    private List<SectionAttendanceDTO> sectionAttendanceList;
}
