package com.wanted.projectmodule2lms.domain.attendance.model.service;

import com.wanted.projectmodule2lms.domain.assignment.model.dao.AssignmentRepository;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.entity.Assignment;
import com.wanted.projectmodule2lms.domain.attendance.model.dao.AttendanceRepository;
import com.wanted.projectmodule2lms.domain.attendance.model.dto.AttendanceCheckResponseDTO;
import com.wanted.projectmodule2lms.domain.attendance.model.dto.AttendancePageDTO;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.Attendance;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.AttendanceStatus;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseDTO;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionDTO;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import com.wanted.projectmodule2lms.domain.section.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private static final int ATTENDANCE_MAX_SCORE = 25;
    private static final int ASSIGNMENT_MAX_SCORE = 25;
    private static final int EXAM_MAX_SCORE = 30;
    private static final int ATTITUDE_MAX_SCORE = 20;
    private static final double LATE_PENALTY_WEIGHT = 0.5;
    private static final double ABSENT_PENALTY_WEIGHT = 1.0;

    private final AttendanceRepository attendanceRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionService sectionService;
    private final CourseService courseService;
    private final AssignmentRepository assignmentRepository;

    public AttendancePageDTO findAttendancePage(Integer memberId, Integer courseId, Integer sectionId) {
        CourseDTO course = courseService.findMyCourseDetail(memberId, courseId);
        SectionDTO section = sectionService.findMySectionDetail(memberId, courseId, sectionId);
        List<SectionDTO> sectionList = sectionService.findMySections(memberId, courseId);
        AssignmentDTO assignment = assignmentRepository.findFirstBySectionIdOrderByAssignmentIdAsc(sectionId)
                .map(this::toAssignmentDTO)
                .orElse(null);

        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        long totalSectionCount = sectionRepository.countByCourseId(courseId);
        List<Attendance> attendanceList = attendanceRepository.findByEnrollmentId(enrollment.getEnrollmentId());
        Map<Integer, String> attendanceStatusMap = buildAttendanceStatusMap(attendanceList);
        int attendanceScore = calculateAttendanceScore(totalSectionCount, attendanceList);

        return new AttendancePageDTO(
                memberId,
                courseId,
                course,
                section,
                sectionList,
                assignment,
                attendanceStatusMap,
                attendanceScore,
                ATTENDANCE_MAX_SCORE,
                0,
                ASSIGNMENT_MAX_SCORE,
                0,
                EXAM_MAX_SCORE,
                0,
                ATTITUDE_MAX_SCORE
        );
    }

    @Transactional
    public AttendanceCheckResponseDTO checkAttendance(Integer memberId, Integer sectionId, LocalDateTime checkedAt) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 섹션이 존재하지 않습니다."));

        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(memberId, section.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        LocalDateTime attendanceTime = checkedAt != null ? checkedAt : LocalDateTime.now();
        validateAttendanceWindow(section.getOpenDate(), attendanceTime);

        AttendanceStatus status = calculateStatus(section.getOpenDate(), attendanceTime);
        String note = status == AttendanceStatus.ABSENT ? "미출석" : null;

        Attendance attendance = attendanceRepository.findByEnrollmentIdAndSectionId(
                enrollment.getEnrollmentId(),
                sectionId
        ).orElse(null);

        if (attendance == null) {
            attendanceRepository.save(new Attendance(
                    enrollment.getEnrollmentId(),
                    sectionId,
                    status,
                    attendanceTime,
                    note
            ));
        } else {
            attendance.changeAttendance(status, attendanceTime, note);
        }

        return new AttendanceCheckResponseDTO(status, buildAttendanceMessage(status));
    }

    private void validateAttendanceWindow(LocalDate openDate, LocalDateTime checkedAt) {
        LocalDateTime presentStart = openDate.atStartOfDay();

        if (checkedAt.isBefore(presentStart)) {
            throw new IllegalArgumentException("아직 출석 가능한 기간이 아닙니다.");
        }
    }

    private AttendanceStatus calculateStatus(LocalDate openDate, LocalDateTime checkedAt) {
        LocalDateTime presentStart = openDate.atStartOfDay();
        LocalDateTime presentEnd = openDate.plusDays(1).atStartOfDay();
        LocalDateTime lateEnd = openDate.plusDays(4).atStartOfDay();

        if (!checkedAt.isBefore(presentStart) && checkedAt.isBefore(presentEnd)) {
            return AttendanceStatus.PRESENT;
        }

        if (!checkedAt.isBefore(presentEnd) && checkedAt.isBefore(lateEnd)) {
            return AttendanceStatus.LATE;
        }

        return AttendanceStatus.ABSENT;
    }

    private Map<Integer, String> buildAttendanceStatusMap(List<Attendance> attendanceList) {
        Map<Integer, String> attendanceStatusMap = new LinkedHashMap<>();

        for (Attendance attendance : attendanceList) {
            attendanceStatusMap.put(attendance.getSectionId(), attendance.getStatus().name());
        }

        return attendanceStatusMap;
    }

    private int calculateAttendanceScore(long totalSectionCount, List<Attendance> attendanceList) {
        if (totalSectionCount <= 0) {
            return ATTENDANCE_MAX_SCORE;
        }

        long lateCount = attendanceList.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE)
                .count();

        long absentCount = attendanceList.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.ABSENT)
                .count();

        double penaltySectionCount = (lateCount * LATE_PENALTY_WEIGHT) + (absentCount * ABSENT_PENALTY_WEIGHT);
        double remainingRatio = Math.max(0.0, 1.0 - (penaltySectionCount / totalSectionCount));

        return (int) Math.round(ATTENDANCE_MAX_SCORE * remainingRatio);
    }

    private String buildAttendanceMessage(AttendanceStatus status) {
        if (status == AttendanceStatus.LATE) {
            return "지각으로 출석 완료되었습니다.";
        }

        if (status == AttendanceStatus.ABSENT) {
            return "결석으로 출석 완료되었습니다.";
        }

        return "출석 완료되었습니다.";
    }

    private AssignmentDTO toAssignmentDTO(Assignment assignment) {
        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getCourseId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getAttachmentFile(),
                assignment.getDueDate()
        );
    }
}