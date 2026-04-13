package com.wanted.projectmodule2lms.domain.grade.model.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeUpdateDTO;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

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

            String completionStatus;
            if (grade.getIsPassed() == null) {
                completionStatus = "미채점";
            } else if (grade.getIsPassed()) {
                completionStatus = "수료";
            } else {
                completionStatus = "미수료";
            }

            GradeDTO gradeDTO = new GradeDTO(
                    grade.getGradeId(),
                    grade.getEnrollmentId(),
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

        BigDecimal totalScore = dto.getAttendanceScore()
                .add(dto.getAssignmentScore())
                .add(dto.getExamScore())
                .add(dto.getAttitudeScore());

        boolean isPassed = totalScore.compareTo(new BigDecimal("60")) >= 0;

        grade.updateScore(
                dto.getAttendanceScore(),
                dto.getAssignmentScore(),
                dto.getExamScore(),
                dto.getAttitudeScore(),
                totalScore,
                isPassed
        );
    }
}