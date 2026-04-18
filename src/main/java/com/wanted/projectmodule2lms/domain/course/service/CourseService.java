package com.wanted.projectmodule2lms.domain.course.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.dto.*;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final SectionRepository sectionRepository;
    private final ResourceLoader resourceLoader;

    private static final Set<String> COURSE_CATEGORIES = Set.of(
            "인문", "사회", "교육", "공학", "자연", "예체능", "기타"
    );

    public List<CourseDTO> findAllCourses() {
        return findAllCourses(null, null);
    }

    public List<CourseDTO> findAllCourses(String keyword, String category) {

        String safeKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String safeCategory = StringUtils.hasText(category) ? category.trim() : null;

        if (safeCategory != null && !isValidCategory(safeCategory)) {
            safeCategory = null;
        }

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
                .filter(course -> course.getApprovalStatus() == CourseApprovalStatus.APPROVED)
                .filter(course -> sectionRepository.countByCourseId(course.getCourseId()) == 8)
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public List<CourseDTO> findCoursesByInstructor(Integer instructorId) {
        List<Course> courseList = courseRepository.findAllByInstructorIdOrderByCourseIdDesc(instructorId);

        return courseList.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public List<CourseDTO> findMyInstructorCourses(String instructorLoginId, String keyword, String category) {

        Member instructor = memberRepository.findByLoginId(instructorLoginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강사 로그인 ID입니다."));

        if (instructor.getRole() != MemberRole.INSTRUCTOR) {
            throw new IllegalArgumentException("강사만 코스 목록을 조회할 수 있습니다.");
        }

        String safeKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String rawCategory = StringUtils.hasText(category) ? category.trim() : null;
        final String safeCategory = (rawCategory != null && isValidCategory(rawCategory)) ? rawCategory : null;

        List<Course> courseList = courseRepository.findAllByInstructorIdOrderByCourseIdDesc(instructor.getMemberId());

        return courseList.stream()
                .filter(course -> course.getApprovalStatus() != CourseApprovalStatus.DELETED)
                .filter(course -> safeKeyword == null || course.getTitle().contains(safeKeyword))
                .filter(course -> safeCategory == null || safeCategory.equals(course.getCategory()))
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
    public Integer registCourse(CourseCreateDTO createDTO, MultipartFile thumbnailFile) throws IOException {

        Member instructor = memberRepository.findByLoginId(createDTO.getInstructorLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강사 로그인 ID입니다."));

        if (instructor.getRole() != MemberRole.INSTRUCTOR) {
            throw new IllegalArgumentException("강사만 코스를 등록할 수 있습니다.");
        }

        if (!isValidCategory(createDTO.getCategory())) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        String thumbnailPath = saveThumbnailFile(thumbnailFile);

        Course course = new Course(
                null,
                instructor.getMemberId(),
                createDTO.getTitle(),
                createDTO.getDescription(),
                createDTO.getCategory(),
                thumbnailPath,
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
    public void modifyCourse(Integer courseId,
                             String instructorLoginId,
                             CourseUpdateDTO updateDTO,
                             MultipartFile thumbnailFile) throws IOException {

        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 코스가 존재하지 않습니다."));

        Member instructor = memberRepository.findByLoginId(instructorLoginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강사 로그인 ID입니다."));

        if (instructor.getRole() != MemberRole.INSTRUCTOR) {
            throw new IllegalArgumentException("강사만 코스를 수정할 수 있습니다.");
        }

        if (!foundCourse.getInstructorId().equals(instructor.getMemberId())) {
            throw new IllegalArgumentException("본인이 등록한 코스만 수정할 수 있습니다.");
        }

        if (!isValidCategory(updateDTO.getCategory())) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        String thumbnailPath = foundCourse.getThumbnailImage();
        String newThumbnailPath = saveThumbnailFile(thumbnailFile);

        if (newThumbnailPath != null) {
            thumbnailPath = newThumbnailPath;
        }

        foundCourse.changeCourseInfo(
                updateDTO.getTitle(),
                updateDTO.getDescription(),
                updateDTO.getCategory(),
                thumbnailPath,
                updateDTO.getCapacity(),
                updateDTO.getStartDate(),
                updateDTO.getEndDate()
        );
    }

    @Transactional
    public void approveCourse(Integer courseId, String adminLoginId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("승인할 코스가 존재하지 않습니다."));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 로그인 ID입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 승인할 수 있습니다.");
        }

        long sectionCount = sectionRepository.countByCourseId(courseId);
        if (sectionCount != 8) {
            throw new IllegalArgumentException("코스를 승인하려면 섹션이 정확히 8개여야 합니다.");
        }

        foundCourse.changeOpenStatus(true);
        foundCourse.approve(admin.getMemberId());
    }

    @Transactional
    public void rejectCourse(Integer courseId, String adminLoginId, String rejectReason) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("반려할 코스가 존재하지 않습니다."));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 로그인 ID입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 반려할 수 있습니다.");
        }

        foundCourse.changeOpenStatus(false);
        foundCourse.reject(admin.getMemberId(), rejectReason);
    }

    @Transactional
    public void deleteCourseByAdmin(Integer courseId, String adminLoginId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 처리할 코스가 존재하지 않습니다."));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 로그인 ID입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 삭제 처리할 수 있습니다.");
        }

        foundCourse.changeOpenStatus(false);
        foundCourse.markDeleted(admin.getMemberId());
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
                .filter(course -> course.getApprovalStatus() == CourseApprovalStatus.APPROVED)
                .filter(course -> Boolean.TRUE.equals(course.getIsOpen()))
                .filter(course -> sectionRepository.countByCourseId(course.getCourseId()) == 8)
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public CourseDTO findMyCourseDetail(Integer memberId, Integer courseId) {

        enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        if (course.getApprovalStatus() != CourseApprovalStatus.APPROVED
                || !Boolean.TRUE.equals(course.getIsOpen())
                || sectionRepository.countByCourseId(courseId) != 8) {
            throw new IllegalArgumentException("접근할 수 없는 코스입니다.");
        }

        return modelMapper.map(course, CourseDTO.class);
    }

    public CourseInstructorDTO findInstructorByCourseId(Integer courseId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        Member instructor = memberRepository.findById(foundCourse.getInstructorId())
                .orElseThrow(() -> new IllegalArgumentException("해당 강사 정보가 존재하지 않습니다."));

        return new CourseInstructorDTO(
                instructor.getLoginId(),
                instructor.getName(),
                instructor.getPhone(),
                instructor.getEmail()
        );
    }

    private boolean isValidCategory(String category) {
        return StringUtils.hasText(category) && COURSE_CATEGORIES.contains(category.trim());
    }

    private String saveThumbnailFile(MultipartFile thumbnailFile) throws IOException {
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            return null;
        }

        Resource resource = resourceLoader.getResource("classpath:static/img/course");
        String filePath;

        if (!resource.exists()) {
            String root = "src/main/resources/static/img/course";
            File file = new File(root);
            file.mkdirs();
            filePath = file.getAbsolutePath();
        } else {
            filePath = resourceLoader
                    .getResource("classpath:static/img/course")
                    .getFile()
                    .getAbsolutePath();
        }

        String originFileName = thumbnailFile.getOriginalFilename();
        String ext = originFileName.substring(originFileName.lastIndexOf("."));
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

        thumbnailFile.transferTo(new File(filePath + "/" + savedName));

        return "static/img/course/" + savedName;
    }

    public CourseAdminDTO findAdminCourseDetail(Integer courseId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        Member instructor = memberRepository.findById(foundCourse.getInstructorId())
                .orElseThrow(() -> new IllegalArgumentException("해당 강사 정보가 존재하지 않습니다."));

        String reviewerLoginId = null;
        String reviewerName = null;

        if (foundCourse.getReviewedBy() != null) {
            Member reviewer = memberRepository.findById(foundCourse.getReviewedBy())
                    .orElseThrow(() -> new IllegalArgumentException("해당 검토자 정보가 존재하지 않습니다."));
            reviewerLoginId = reviewer.getLoginId();
            reviewerName = reviewer.getName();
        }

        return new CourseAdminDTO(
                foundCourse.getTitle(),
                foundCourse.getDescription(),
                foundCourse.getCategory(),
                foundCourse.getThumbnailImage(),
                foundCourse.getCapacity(),
                foundCourse.getStartDate(),
                foundCourse.getEndDate(),
                foundCourse.getIsOpen(),
                foundCourse.getApprovalStatus().name(),
                foundCourse.getRejectReason(),
                instructor.getLoginId(),
                instructor.getName(),
                reviewerLoginId,
                reviewerName
        );
    }


    public String getCourseNameById(Integer courseId) {
        return courseRepository.findById(courseId)
                .map(Course::getTitle)
                .orElse(null);
    }
}
