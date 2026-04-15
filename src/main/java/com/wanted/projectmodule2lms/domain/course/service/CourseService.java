package com.wanted.projectmodule2lms.domain.course.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseCreateDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseStudentDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseUpdateDTO;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final SectionRepository sectionRepository;

    public List<CourseDTO> findAllCourses() {
        return findAllCourses(null, null);
    }

    public List<CourseDTO> findAllCourses(String keyword, String category) {

        String safeKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String safeCategory = StringUtils.hasText(category) ? category.trim() : null;

        List<Course> courseList;

        if (safeKeyword != null && safeCategory != null) {
            courseList = courseRepository
                    .findByApprovalStatusNotAndTitleContainingAndCategoryContainingOrderByCourseIdDesc(
                            CourseApprovalStatus.DELETED,
                            safeKeyword,
                            safeCategory
                    );
        } else if (safeKeyword != null) {
            courseList = courseRepository
                    .findByApprovalStatusNotAndTitleContainingOrderByCourseIdDesc(
                            CourseApprovalStatus.DELETED,
                            safeKeyword
                    );
        } else if (safeCategory != null) {
            courseList = courseRepository
                    .findByApprovalStatusNotAndCategoryContainingOrderByCourseIdDesc(
                            CourseApprovalStatus.DELETED,
                            safeCategory
                    );
        } else {
            courseList = courseRepository
                    .findByApprovalStatusNotOrderByCourseIdDesc(CourseApprovalStatus.DELETED);
        }

        return courseList.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public List<CourseDTO> findOpenCourses() {
        List<Course> courseList = courseRepository.findAllByIsOpenTrueOrderByCourseIdDesc();

        return courseList.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public List<CourseDTO> findCoursesByInstructor(Integer instructorId) {
        List<Course> courseList = courseRepository.findAllByInstructorIdOrderByCourseIdDesc(instructorId);

        return courseList.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public CourseDTO findCourseById(Integer courseId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        return modelMapper.map(foundCourse, CourseDTO.class);
    }

    public List<CourseStudentDTO> findStudentsByCourseId(Integer courseId) {

        List<Enrollment> enrollmentList = enrollmentRepository.findByCourseIdOrderByEnrolledAtAsc(courseId);

        if (enrollmentList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> memberIds = enrollmentList.stream()
                .map(Enrollment::getMemberId)
                .toList();

        List<Member> memberList = memberRepository.findByMemberIdIn(memberIds);

        Map<Integer, Member> memberMap = new HashMap<>();
        for (Member member : memberList) {
            memberMap.put(member.getMemberId(), member);
        }

        List<CourseStudentDTO> studentList = new ArrayList<>();

        for (Enrollment enrollment : enrollmentList) {
            Member member = memberMap.get(enrollment.getMemberId());

            if (member != null) {
                CourseStudentDTO dto = new CourseStudentDTO(
                        enrollment.getEnrollmentId(),
                        member.getMemberId(),
                        member.getLoginId(),
                        member.getName(),
                        member.getPhone(),
                        member.getEmail(),
                        enrollment.getStatus().name(),
                        enrollment.getEnrolledAt()
                );

                studentList.add(dto);
            }
        }

        return studentList;
    }

    @Transactional
    public Integer registCourse(CourseCreateDTO createDTO) {
        Course course = new Course(
                createDTO.getCourseId(),
                createDTO.getInstructorId(),
                createDTO.getTitle(),
                createDTO.getDescription(),
                createDTO.getCategory(),
                createDTO.getThumbnailImage(),
                createDTO.getCapacity(),
                createDTO.getStartDate(),
                createDTO.getEndDate(),
                false,
                CourseApprovalStatus.PENDING,
                null,
                null,
                null,
                null,
                null
        );

        courseRepository.save(course);
        return course.getCourseId();
    }

    @Transactional
    public void modifyCourse(Integer courseId, CourseUpdateDTO updateDTO) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 코스가 존재하지 않습니다."));

        foundCourse.changeCourseInfo(
                updateDTO.getTitle(),
                updateDTO.getDescription(),
                updateDTO.getCategory(),
                updateDTO.getThumbnailImage(),
                updateDTO.getCapacity(),
                updateDTO.getStartDate(),
                updateDTO.getEndDate()
        );
    }

    @Transactional
    public void approveCourse(Integer courseId, Integer adminId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("승인할 코스가 존재하지 않습니다."));

        long sectionCount = sectionRepository.countByCourseId(courseId);
        if (sectionCount != 8) {
            throw new IllegalArgumentException("코스를 승인하려면 섹션이 정확히 8개여야 합니다.");
        }

        foundCourse.approve(adminId);
    }

    @Transactional
    public void rejectCourse(Integer courseId, Integer adminId, String rejectReason) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("반려할 코스가 존재하지 않습니다."));

        foundCourse.reject(adminId, rejectReason);
    }

    @Transactional
    public void deleteCourseByAdmin(Integer courseId, Integer adminId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 처리할 코스가 존재하지 않습니다."));

        foundCourse.markDeleted(adminId);
    }

    public List<CourseDTO> findMyCourses(Integer memberId) {

        List<Enrollment> enrollmentList = enrollmentRepository.findByMemberId(memberId);

        List<Integer> courseIds = enrollmentList.stream()
                .map(Enrollment::getCourseId)
                .toList();

        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Course> courseList = courseRepository.findAllById(courseIds);

        return courseList.stream()
                .filter(course -> course.getApprovalStatus() != CourseApprovalStatus.DELETED)
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public CourseDTO findMyCourseDetail(Integer memberId, Integer courseId) {

        enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        if (course.getApprovalStatus() != CourseApprovalStatus.APPROVED || !Boolean.TRUE.equals(course.getIsOpen())) {
            throw new IllegalArgumentException("접근할 수 없는 코스입니다.");
        }

        return modelMapper.map(course, CourseDTO.class);
    }

}