package com.wanted.projectmodule2lms.domain.attendance.model.dto;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class AttendancePageDTO {

    private Integer memberId;
    private Integer enrollmentId;
    private Integer courseId;
    private CourseDTO course;
    private SectionDTO section;
    private List<SectionDTO> sectionList;
    private AssignmentDTO assignment;
    private Map<Integer, String> attendanceStatusMap;
    private Integer attendanceScore;
    private Integer attendanceMaxScore;
    private Integer assignmentScore;
    private Integer assignmentMaxScore;
    private Integer examScore;
    private Integer examMaxScore;
    private Integer attitudeScore;
    private Integer attitudeMaxScore;
    private Integer totalScore;
    private String certificateStatus;

}