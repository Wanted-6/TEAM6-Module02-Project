package com.wanted.projectmodule2lms.domain.grade.model.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}