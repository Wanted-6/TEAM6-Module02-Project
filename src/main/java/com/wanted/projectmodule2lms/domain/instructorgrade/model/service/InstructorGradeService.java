package com.wanted.projectmodule2lms.domain.instructorgrade.model.service;


import com.wanted.projectmodule2lms.domain.attendance.model.dao.AttendanceRepository;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.Attendance;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.AttendanceStatus;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import com.wanted.projectmodule2lms.domain.instructorgrade.model.dto.InstructorStudentManageDTO;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionAttendanceDTO;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorGradeService {
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    public List<InstructorStudentManageDTO> findStudentsByCourse(Integer courseId) {
        List<InstructorStudentManageDTO> studentList = new ArrayList<>();

        List<Enrollment> enrollmentList = enrollmentRepository.findByCourseId(courseId);
        List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);

        for (Enrollment enrollment : enrollmentList) {
            Member member = memberRepository.findById(enrollment.getMemberId()).orElse(null);
            Grade grade = gradeRepository.findByEnrollmentId(enrollment.getEnrollmentId()).orElse(null);
            List<Attendance> attendanceList =
                    attendanceRepository.findByEnrollmentIdOrderBySectionIdAsc(enrollment.getEnrollmentId());

            if (member == null) {
                continue;
            }

            InstructorStudentManageDTO dto = new InstructorStudentManageDTO();
            dto.setMemberId(member.getMemberId());
            dto.setEnrollmentId(enrollment.getEnrollmentId());
            dto.setStudentName(member.getName());
            dto.setEmail(member.getEmail());
            dto.setProgressRate(calculateProgressRate(sectionList.size(), attendanceList.size()));
            dto.setAttendanceSummary(makeAttendanceSummary(attendanceList));
            dto.setAssignmentScore(
                    grade != null && grade.getAssignmentScore() != null ? grade.getAssignmentScore().doubleValue() : null
            );
            dto.setExamScore(
                    grade != null && grade.getExamScore() != null ? grade.getExamScore().doubleValue() : null
            );
            dto.setAttitudeScore(
                    grade != null && grade.getAttitudeScore() != null ? grade.getAttitudeScore().doubleValue() : null
            );
            dto.setSectionAttendanceList(makeSectionAttendanceList(sectionList, attendanceList));
            studentList.add(dto);
        }

        return studentList;
    }

    @Transactional
    public void updateAttendanceStatus(Integer instructorId,
                                       Integer enrollmentId,
                                       Integer sectionId,
                                       String status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 정보가 없습니다."));

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("섹션 정보가 없습니다."));

        if (!enrollment.getCourseId().equals(section.getCourseId())) {
            throw new IllegalArgumentException("코스 정보가 일치하지 않습니다.");
        }

        Course course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("코스 정보가 없습니다."));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new IllegalArgumentException("해당 강의 담당 강사만 출결을 수정할 수 있습니다.");
        }

        AttendanceStatus attendanceStatus = AttendanceStatus.valueOf(status);

        Attendance attendance = attendanceRepository.findByEnrollmentIdAndSectionId(enrollmentId, sectionId)
                .orElse(null);

        if (attendance == null) {
            attendanceRepository.save(new Attendance(
                    enrollmentId,
                    sectionId,
                    attendanceStatus,
                    LocalDateTime.now(),
                    "강사 변경"
            ));
            return;
        }

        attendance.changeAttendance(attendanceStatus, LocalDateTime.now(), "강사 변경");
    }

    private Integer calculateProgressRate(int totalSectionCount, int attendedCount) {
        if (totalSectionCount == 0) {
            return 0;
        }

        return (attendedCount * 100) / totalSectionCount;
    }

    private String makeAttendanceSummary(List<Attendance> attendanceList) {
        int presentCount = 0;
        int lateCount = 0;
        int absentCount = 0;

        for (Attendance attendance : attendanceList) {
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                presentCount++;
            } else if (attendance.getStatus() == AttendanceStatus.LATE) {
                lateCount++;
            } else if (attendance.getStatus() == AttendanceStatus.ABSENT) {
                absentCount++;
            }
        }

        return "출석 " + presentCount + " / 지각 " + lateCount + " / 결석 " + absentCount;
    }

    private List<SectionAttendanceDTO> makeSectionAttendanceList(List<Section> sectionList,
                                                                 List<Attendance> attendanceList) {
        List<SectionAttendanceDTO> sectionAttendanceList = new ArrayList<>();

        for (Section section : sectionList) {
            SectionAttendanceDTO sectionAttendanceDTO = new SectionAttendanceDTO();
            sectionAttendanceDTO.setSectionId(section.getSectionId());
            sectionAttendanceDTO.setSectionTitle(section.getTitle());
            sectionAttendanceDTO.setStatus(findAttendanceStatus(section, attendanceList));
            sectionAttendanceList.add(sectionAttendanceDTO);
        }

        return sectionAttendanceList;
    }

    private String findAttendanceStatus(Section section, List<Attendance> attendanceList) {
        for (Attendance attendance : attendanceList) {
            if (attendance.getSectionId().equals(section.getSectionId())) {
                return attendance.getStatus().name();
            }
        }

        if (section.getOpenDate() != null && section.getOpenDate().isAfter(LocalDate.now())) {
            return "NOT_OPEN";
        }

        return "UNCHECKED";
    }
}
