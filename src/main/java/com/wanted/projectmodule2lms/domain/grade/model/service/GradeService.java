package com.wanted.projectmodule2lms.domain.grade.model.service;

import com.wanted.projectmodule2lms.domain.attendance.model.dao.AttendanceRepository;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.Attendance;
import com.wanted.projectmodule2lms.domain.attendance.model.entity.AttendanceStatus;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeUpdateDTO;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedInstructorException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private static final BigDecimal PERFECT_ATTENDANCE_SCORE = new BigDecimal("100.00");
    private static final BigDecimal ABSENT_PENALTY = new BigDecimal("10.00");
    private static final BigDecimal LATE_PENALTY = new BigDecimal("5.00");

    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public List<GradeDTO> findGradesByMemberId(Integer memberId) {

        List<Enrollment> enrollments = enrollmentRepository.findByMemberId(memberId);
        List<GradeDTO> gradeDTOList = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Grade grade = gradeRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                    .orElse(null);

            if (grade == null) {
                continue;
            }

            Course course = courseRepository.findById(enrollment.getCourseId())
                    .orElse(null);

            String courseTitle = (course != null) ? course.getTitle() : "과목명 없음";

            String studentName = memberRepository.findById(enrollment.getMemberId())
                    .map(member -> member.getName())
                    .orElse("이름 없음");

            String completionStatus = getCompletionStatus(grade);

            GradeDTO gradeDTO = new GradeDTO(
                    grade.getGradeId(),
                    grade.getEnrollmentId(),
                    studentName,
                    courseTitle,
                    grade.getAttendanceScore(),
                    grade.getAssignmentScore(),
                    grade.getExamScore(),
                    grade.getAttitudeScore(),
                    grade.getTotalScore(),
                    completionStatus
            );

            gradeDTOList.add(gradeDTO);
        }

        return gradeDTOList;
    }

    @Transactional
    public void updateGradeByInstructor(Integer instructorId, GradeUpdateDTO dto) {

        Enrollment enrollment = enrollmentRepository.findById(dto.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수강 정보입니다."));

        Course course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new IllegalArgumentException("해당 강의의 담당 강사만 성적을 수정할 수 있습니다.");
        }

        Grade grade = gradeRepository.findByEnrollmentId(dto.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("성적 정보가 존재하지 않습니다."));

        BigDecimal attendanceScore = calculateAttendanceScore(dto.getEnrollmentId());
        BigDecimal assignmentScore = dto.getAssignmentScore();
        BigDecimal examScore = dto.getExamScore();
        BigDecimal attitudeScore = dto.getAttitudeScore();

        BigDecimal totalScore = calculateTotalScore(
                attendanceScore,
                assignmentScore,
                examScore,
                attitudeScore
        );

        boolean isPassed = totalScore.compareTo(new BigDecimal("60")) >= 0;

        grade.updateScore(
                attendanceScore,
                assignmentScore,
                examScore,
                attitudeScore,
                totalScore,
                isPassed
        );
    }

    public List<GradeDTO> findGradesByInstructorId(Integer instructorId) {

        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        List<GradeDTO> gradeDTOList = new ArrayList<>();

        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getCourseId());

            for (Enrollment enrollment : enrollments) {
                Grade grade = gradeRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                        .orElse(null);

                if (grade == null) {
                    continue;
                }

                String studentName = memberRepository.findById(enrollment.getMemberId())
                        .map(member -> member.getName())
                        .orElse("이름 없음");

                String completionStatus = getCompletionStatus(grade);

                GradeDTO gradeDTO = new GradeDTO(
                        grade.getGradeId(),
                        grade.getEnrollmentId(),
                        studentName,
                        course.getTitle(),
                        grade.getAttendanceScore(),
                        grade.getAssignmentScore(),
                        grade.getExamScore(),
                        grade.getAttitudeScore(),
                        grade.getTotalScore(),
                        completionStatus
                );

                gradeDTOList.add(gradeDTO);
            }
        }

        return gradeDTOList;
    }

    public GradeDTO findGradeByEnrollmentIdForInstructor(Integer instructorId, Integer enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 수강 정보입니다."));

        Course course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 강의입니다."));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedInstructorException("해당 강의의 담당 강사만 조회할 수 있습니다.");
        }

        Grade grade = gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("성적 정보가 존재하지 않습니다."));

        String studentName = memberRepository.findById(enrollment.getMemberId())
                .map(member -> member.getName())
                .orElse("이름 없음");

        String completionStatus = getCompletionStatus(grade);

        return new GradeDTO(
                grade.getGradeId(),
                grade.getEnrollmentId(),
                studentName,
                course.getTitle(),
                grade.getAttendanceScore(),
                grade.getAssignmentScore(),
                grade.getExamScore(),
                grade.getAttitudeScore(),
                grade.getTotalScore(),
                completionStatus
        );
    }

    private String getCompletionStatus(Grade grade) {
        if (grade.getIsPassed() == null) {
            return "미채점";
        } else if (grade.getIsPassed()) {
            return "수료";
        } else {
            return "미수료";
        }
    }

    private BigDecimal calculateAttendanceScore(Integer enrollmentId) {
        List<Attendance> attendances = attendanceRepository.findByEnrollmentId(enrollmentId);

        BigDecimal totalPenalty = attendances.stream()
                .map(this::calculatePenalty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal attendanceScore = PERFECT_ATTENDANCE_SCORE.subtract(totalPenalty);

        if (attendanceScore.compareTo(BigDecimal.ZERO) < 0) {
            attendanceScore = BigDecimal.ZERO;
        }

        return attendanceScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePenalty(Attendance attendance) {
        if (attendance.getStatus() == AttendanceStatus.ABSENT) {
            return ABSENT_PENALTY;
        }

        if (attendance.getStatus() == AttendanceStatus.LATE) {
            return LATE_PENALTY;
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateTotalScore(
            BigDecimal attendanceScore,
            BigDecimal assignmentScore,
            BigDecimal examScore,
            BigDecimal attitudeScore
    ) {
        return attendanceScore.multiply(new BigDecimal("0.25"))
                .add(assignmentScore.multiply(new BigDecimal("0.25")))
                .add(examScore.multiply(new BigDecimal("0.30")))
                .add(attitudeScore.multiply(new BigDecimal("0.20")))
                .setScale(2, RoundingMode.HALF_UP);
    }


}
