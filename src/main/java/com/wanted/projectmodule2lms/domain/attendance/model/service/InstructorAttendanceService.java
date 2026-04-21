package com.wanted.projectmodule2lms.domain.attendance.model.service;

import com.wanted.projectmodule2lms.domain.attendance.model.dao.AttendanceRepository;
import com.wanted.projectmodule2lms.domain.attendance.model.dto.InstructorAttendanceManageDTO;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.Attendance;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.AttendanceStatus;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorAttendanceService {

    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final GradeService gradeService;

    public List<InstructorAttendanceManageDTO> findAttendanceManageStudentsByCourse(Integer courseId) {
        List<InstructorAttendanceManageDTO> studentList = new ArrayList<>();

        List<Enrollment> enrollmentList = enrollmentRepository.findByCourseId(courseId);
        List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);

        Set<Integer> memberIds = enrollmentList.stream()
                .map(Enrollment::getMemberId)
                .collect(Collectors.toSet());

        Map<Integer, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, member -> member));

        List<Integer> enrollmentIds = enrollmentList.stream()
                .map(Enrollment::getEnrollmentId)
                .toList();

        Map<Integer, Grade> gradeMap = gradeRepository.findByEnrollmentIdIn(enrollmentIds).stream()
                .collect(Collectors.toMap(Grade::getEnrollmentId, grade -> grade));

        List<Attendance> allAttendances = attendanceRepository
                .findByEnrollmentIdInOrderByEnrollmentIdAscSectionIdAsc(enrollmentIds);

        Map<Integer, List<Attendance>> attendanceMap = allAttendances.stream()
                .collect(Collectors.groupingBy(Attendance::getEnrollmentId));

        for (Enrollment enrollment : enrollmentList) {
            Member member = memberMap.get(enrollment.getMemberId());
            Grade grade = gradeMap.get(enrollment.getEnrollmentId());
            List<Attendance> attendanceList = findLatestAttendances(
                    attendanceMap.getOrDefault(enrollment.getEnrollmentId(), Collections.emptyList())
            );

            if (member == null) {
                continue;
            }

            InstructorAttendanceManageDTO dto = new InstructorAttendanceManageDTO(
                    member.getMemberId(),
                    enrollment.getEnrollmentId(),
                    member.getName(),
                    member.getEmail(),
                    calculateProgressRate(sectionList.size(), attendanceList.size()),
                    buildAttendanceSummary(attendanceList),
                    grade != null && grade.getAssignmentScore() != null ? grade.getAssignmentScore().doubleValue() : null,
                    grade != null && grade.getExamScore() != null ? grade.getExamScore().doubleValue() : null,
                    grade != null && grade.getAttitudeScore() != null ? grade.getAttitudeScore().doubleValue() : null,
                    null,
                    buildSectionAttendanceList(sectionList, attendanceList)
            );
            studentList.add(dto);
        }

        return studentList;
    }

    private List<Attendance> findLatestAttendances(List<Attendance> attendances) {
        Map<Integer, Attendance> latestAttendanceBySection = new LinkedHashMap<>();

        for (Attendance attendance : attendances) {
            Attendance current = latestAttendanceBySection.get(attendance.getSectionId());

            if (current == null || ATTENDANCE_ORDER.compare(attendance, current) > 0) {
                latestAttendanceBySection.put(attendance.getSectionId(), attendance);
            }
        }

        return new ArrayList<>(latestAttendanceBySection.values());
    }


    @Transactional
    public void updateAttendanceStatusByInstructor(Integer instructorId,
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
        } else {
            attendance.changeAttendance(attendanceStatus, LocalDateTime.now(), "강사 변경");
        }

        gradeService.refreshGradeScoresByEnrollmentId(enrollmentId);
    }

    private Integer calculateProgressRate(int totalSectionCount, int attendedCount) {
        if (totalSectionCount == 0) {
            return 0;
        }

        return (attendedCount * 100) / totalSectionCount;
    }

    private String buildAttendanceSummary(List<Attendance> attendanceList) {
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

    private List<SectionAttendanceDTO> buildSectionAttendanceList(List<Section> sectionList,
                                                                  List<Attendance> attendanceList) {
        List<SectionAttendanceDTO> sectionAttendanceList = new ArrayList<>();

        for (Section section : sectionList) {
            SectionAttendanceDTO sectionAttendanceDTO = new SectionAttendanceDTO(
                    section.getSectionId(),
                    section.getTitle(),
                    findAttendanceStatus(section, attendanceList)
            );
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

    private List<Attendance> findLatestAttendancesByEnrollmentId(Integer enrollmentId) {
        List<Attendance> attendances = attendanceRepository.findByEnrollmentIdOrderBySectionIdAsc(enrollmentId);
        Map<Integer, Attendance> latestAttendanceBySection = new LinkedHashMap<>();

        for (Attendance attendance : attendances) {
            Attendance current = latestAttendanceBySection.get(attendance.getSectionId());

            if (current == null || ATTENDANCE_ORDER.compare(attendance, current) > 0) {
                latestAttendanceBySection.put(attendance.getSectionId(), attendance);
            }
        }

        return new ArrayList<>(latestAttendanceBySection.values());
    }

    private static final Comparator<Attendance> ATTENDANCE_ORDER =
            Comparator.comparing(Attendance::getCheckedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(Attendance::getRecordedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(Attendance::getAttendanceId, Comparator.nullsFirst(Comparator.naturalOrder()));
}
