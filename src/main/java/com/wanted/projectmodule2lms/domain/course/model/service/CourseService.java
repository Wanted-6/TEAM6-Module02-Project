package com.wanted.projectmodule2lms.domain.course.model.service;

import com.wanted.projectmodule2lms.domain.assignment.model.dao.AssignmentRepository;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseAdminDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseAdminListDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseCreateDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseInstructorDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseStudentDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseUpdateDTO;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedInstructorException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedStudentAccessException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final AssignmentRepository assignmentRepository;
    private final ModelMapper modelMapper;
    private final SectionRepository sectionRepository;
    private final ResourceLoader resourceLoader;

    private static final Set<String> COURSE_CATEGORIES = Set.of(
            "Backend", "Database", "AI", "Infra", "DevOps", "Data"
    );

    public List<CourseDTO> findAllCourses() {
        return findAllCourses(null, null);
    }

    public List<CourseDTO> findAllCourses(String keyword, String category) {
        List<Course> courseList = findVisibleCourses(keyword, category);

        return courseList.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public List<CourseDTO> findOpenCourses() {
        return findOpenCourses(null, null);
    }

    public List<CourseDTO> findOpenCourses(String keyword, String category) {
        List<Course> courseList = findVisibleCourses(keyword, category);

        List<Integer> courseIds = courseList.stream()
                .map(Course::getCourseId)
                .toList();

        Set<Integer> completedCourseIds = findCompletedCourseIdSet(courseIds);

        return courseList.stream()
                .filter(course -> course.getApprovalStatus() == CourseApprovalStatus.APPROVED)
                .filter(course -> Boolean.TRUE.equals(course.getIsOpen()))
                .filter(course -> completedCourseIds.contains(course.getCourseId()))
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
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 강사 로그인 ID입니다."));

        if (instructor.getRole() != MemberRole.INSTRUCTOR) {
            throw new UnauthorizedInstructorException("강사만 코스 목록을 조회할 수 있습니다.");
        }

        String safeKeyword = normalizeKeyword(keyword);
        String safeCategory = normalizeCategory(category);

        List<Course> courseList;

        if (safeKeyword != null && safeCategory != null) {
            courseList = courseRepository
                    .findByInstructorIdAndApprovalStatusNotAndTitleContainingAndCategoryOrderByCourseIdDesc(
                            instructor.getMemberId(),
                            CourseApprovalStatus.DELETED,
                            safeKeyword,
                            safeCategory
                    );
        } else if (safeKeyword != null) {
            courseList = courseRepository
                    .findByInstructorIdAndApprovalStatusNotAndTitleContainingOrderByCourseIdDesc(
                            instructor.getMemberId(),
                            CourseApprovalStatus.DELETED,
                            safeKeyword
                    );
        } else if (safeCategory != null) {
            courseList = courseRepository
                    .findByInstructorIdAndApprovalStatusNotAndCategoryOrderByCourseIdDesc(
                            instructor.getMemberId(),
                            CourseApprovalStatus.DELETED,
                            safeCategory
                    );
        } else {
            courseList = courseRepository
                    .findByInstructorIdAndApprovalStatusNotOrderByCourseIdDesc(
                            instructor.getMemberId(),
                            CourseApprovalStatus.DELETED
                    );
        }

        return courseList.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public CourseDTO findCourseById(Integer courseId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 코스가 존재하지 않습니다."));

        return modelMapper.map(foundCourse, CourseDTO.class);
    }

    public boolean hasAssignmentByCourseId(Integer courseId) {
        return assignmentRepository.existsByCourseId(courseId);
    }

    public List<CourseStudentDTO> findStudentsByCourseId(Integer courseId) {
        List<Enrollment> enrollmentList = enrollmentRepository.findByCourseIdOrderByEnrolledAtAsc(courseId);

        if (enrollmentList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> memberIds = enrollmentList.stream()
                .map(Enrollment::getMemberId)
                .toList();

        Map<Integer, Member> memberMap = findMemberMapByIds(memberIds);

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
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 강사 로그인 ID입니다."));

        if (instructor.getRole() != MemberRole.INSTRUCTOR) {
            throw new UnauthorizedInstructorException("강사만 코스를 등록할 수 있습니다.");
        }

        String normalizedCategory = normalizeCategory(createDTO.getCategory());
        if (normalizedCategory == null) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        String thumbnailPath = saveThumbnailFile(thumbnailFile);

        Course course = new Course(
                null,
                instructor.getMemberId(),
                createDTO.getTitle(),
                createDTO.getDescription(),
                normalizedCategory,
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
                .orElseThrow(() -> new ResourceNotFoundException("수정할 코스가 존재하지 않습니다."));

        Member instructor = memberRepository.findByLoginId(instructorLoginId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 강사 로그인 ID입니다."));

        if (instructor.getRole() != MemberRole.INSTRUCTOR) {
            throw new UnauthorizedInstructorException("강사만 코스를 수정할 수 있습니다.");
        }

        if (!foundCourse.getInstructorId().equals(instructor.getMemberId())) {
            throw new UnauthorizedInstructorException("본인이 등록한 코스만 수정할 수 있습니다.");
        }

        String normalizedCategory = normalizeCategory(updateDTO.getCategory());
        if (normalizedCategory == null) {
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
                normalizedCategory,
                thumbnailPath,
                updateDTO.getCapacity(),
                updateDTO.getStartDate(),
                updateDTO.getEndDate()
        );
    }

    @Transactional
    public void approveCourse(Integer courseId, String adminLoginId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("승인할 코스가 존재하지 않습니다."));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 관리자 로그인 ID입니다."));

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
                .orElseThrow(() -> new ResourceNotFoundException("반려할 코스가 존재하지 않습니다."));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 관리자 로그인 ID입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 반려할 수 있습니다.");
        }

        foundCourse.changeOpenStatus(false);
        foundCourse.reject(admin.getMemberId(), rejectReason);
    }

    @Transactional
    public void deleteCourseByAdmin(Integer courseId, String adminLoginId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("삭제 처리할 코스가 존재하지 않습니다."));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 관리자 로그인 ID입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 삭제 처리할 수 있습니다.");
        }

        foundCourse.changeOpenStatus(false);
        foundCourse.markDeleted(admin.getMemberId());
    }

    public List<CourseAdminListDTO> findAdminCourseList() {
        List<Course> courseList = courseRepository.findByApprovalStatusNotOrderByCourseIdDesc(CourseApprovalStatus.DELETED);

        if (courseList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> instructorIds = courseList.stream()
                .map(Course::getInstructorId)
                .distinct()
                .toList();

        Map<Integer, Member> instructorMap = findMemberMapByIds(instructorIds);

        return courseList.stream()
                .map(course -> {
                    Member instructor = instructorMap.get(course.getInstructorId());

                    if (instructor == null) {
                        throw new ResourceNotFoundException("강사 정보가 존재하지 않습니다.");
                    }

                    return new CourseAdminListDTO(
                            course.getCourseId(),
                            course.getTitle(),
                            instructor.getLoginId(),
                            instructor.getName(),
                            course.getApprovalStatus().name(),
                            course.getIsOpen()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<CourseDTO> findMyCourses(Integer memberId) {
        List<Integer> courseIds = enrollmentRepository.findCourseIdsByMemberId(memberId);

        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Course> courseList = courseRepository.findByCourseIdIn(courseIds);
        Set<Integer> completedCourseIds = findCompletedCourseIdSet(courseIds);

        return courseList.stream()
                .filter(course -> course.getApprovalStatus() == CourseApprovalStatus.APPROVED)
                .filter(course -> Boolean.TRUE.equals(course.getIsOpen()))
                .filter(course -> completedCourseIds.contains(course.getCourseId()))
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }


    public CourseDTO findMyCourseDetail(Integer memberId, Integer courseId) {
        enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new UnauthorizedStudentAccessException("수강 중인 코스가 아닙니다."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 코스가 존재하지 않습니다."));

        if (course.getApprovalStatus() != CourseApprovalStatus.APPROVED
                || !Boolean.TRUE.equals(course.getIsOpen())
                || sectionRepository.countByCourseId(courseId) != 8) {
            throw new UnauthorizedStudentAccessException("접근할 수 없는 코스입니다.");
        }

        return modelMapper.map(course, CourseDTO.class);
    }

    public CourseInstructorDTO findInstructorByCourseId(Integer courseId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 코스가 존재하지 않습니다."));

        Member instructor = memberRepository.findById(foundCourse.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 강사 정보가 존재하지 않습니다."));

        return new CourseInstructorDTO(
                instructor.getLoginId(),
                instructor.getName(),
                instructor.getPhone(),
                instructor.getEmail()
        );
    }

    public List<String> getCourseCategories() {
        return new ArrayList<>(new LinkedHashSet<>(COURSE_CATEGORIES));
    }

    public CourseAdminDTO findAdminCourseDetail(Integer courseId) {
        Course foundCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 코스가 존재하지 않습니다."));

        Member instructor = memberRepository.findById(foundCourse.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 강사 정보가 존재하지 않습니다."));

        String reviewerLoginId = null;
        String reviewerName = null;

        if (foundCourse.getReviewedBy() != null) {
            Member reviewer = memberRepository.findById(foundCourse.getReviewedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("해당 검토자 정보가 존재하지 않습니다."));
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

    private List<Course> findVisibleCourses(String keyword, String category) {
        String safeKeyword = normalizeKeyword(keyword);
        String safeCategory = normalizeCategory(category);

        if (safeKeyword != null && safeCategory != null) {
            return courseRepository
                    .findByApprovalStatusNotAndTitleContainingAndCategoryContainingOrderByCourseIdDesc(
                            CourseApprovalStatus.DELETED,
                            safeKeyword,
                            safeCategory
                    );
        }

        if (safeKeyword != null) {
            return courseRepository
                    .findByApprovalStatusNotAndTitleContainingOrderByCourseIdDesc(
                            CourseApprovalStatus.DELETED,
                            safeKeyword
                    );
        }

        if (safeCategory != null) {
            return courseRepository
                    .findByApprovalStatusNotAndCategoryContainingOrderByCourseIdDesc(
                            CourseApprovalStatus.DELETED,
                            safeCategory
                    );
        }

        return courseRepository.findByApprovalStatusNotOrderByCourseIdDesc(CourseApprovalStatus.DELETED);
    }

    private Set<Integer> findCompletedCourseIdSet(List<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Set.of();
        }

        List<Section> sectionList = sectionRepository.findByCourseIdIn(courseIds);

        Map<Integer, Long> sectionCountMap = sectionList.stream()
                .collect(Collectors.groupingBy(Section::getCourseId, Collectors.counting()));

        return sectionCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() == 8L)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Map<Integer, Member> findMemberMapByIds(List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Member> memberList = memberRepository.findByMemberIdIn(memberIds);

        Map<Integer, Member> memberMap = new HashMap<>();
        for (Member member : memberList) {
            memberMap.put(member.getMemberId(), member);
        }

        return memberMap;
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }

        String trimmedCategory = category.trim();

        for (String courseCategory : COURSE_CATEGORIES) {
            if (courseCategory.equalsIgnoreCase(trimmedCategory)) {
                return courseCategory;
            }
        }

        return null;
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

        return "img/course/" + savedName;
    }
}
