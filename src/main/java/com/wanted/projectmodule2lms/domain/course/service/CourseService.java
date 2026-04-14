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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<CourseDTO> findAllCourses() {
        List<Course> courseList = courseRepository.findAllByOrderByCourseIdDesc();

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
}