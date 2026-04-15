package com.wanted.projectmodule2lms.domain.section.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionCreateDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionUpdateDTO;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;

    public List<SectionDTO> findSectionsByCourseId(Integer courseId) {
        List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);

        return sectionList.stream()
                .map(section -> modelMapper.map(section, SectionDTO.class))
                .collect(Collectors.toList());
    }

    public SectionDTO findSectionById(Integer sectionId) {
        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 섹션이 존재하지 않습니다."));

        return modelMapper.map(foundSection, SectionDTO.class);
    }

    @Transactional
    public Integer registSection(Integer courseId, SectionCreateDTO createDTO) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("부모 코스가 존재하지 않습니다.");
        }

        if (sectionRepository.existsByCourseIdAndSectionOrder(courseId, createDTO.getSectionOrder())) {
            throw new IllegalArgumentException("같은 코스 안에 중복된 섹션 순서가 존재합니다.");
        }

        if (sectionRepository.countByCourseId(courseId) >= 8) {
            throw new IllegalArgumentException("한 코스의 섹션은 8개까지만 등록할 수 있습니다.");
        }

        Section section = new Section(
                createDTO.getSectionId(),
                courseId,
                createDTO.getTitle(),
                createDTO.getVideoUrl(),
                createDTO.getMaterialFile(),
                createDTO.getSectionOrder(),
                createDTO.getOpenDate()
        );

        sectionRepository.save(section);
        return section.getSectionId();
    }

    @Transactional
    public void modifySection(Integer sectionId, SectionUpdateDTO updateDTO) {
        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 섹션이 존재하지 않습니다."));

        if (sectionRepository.existsByCourseIdAndSectionOrderAndSectionIdNot(
                foundSection.getCourseId(),
                updateDTO.getSectionOrder(),
                sectionId
        )) {
            throw new IllegalArgumentException("같은 코스 안에 중복된 섹션 순서가 존재합니다.");
        }

        if (updateDTO.getSectionOrder() < 1 || updateDTO.getSectionOrder() > 8) {
            throw new IllegalArgumentException("섹션 순서는 1부터 8까지만 가능합니다.");
        }

        foundSection.changeSectionInfo(
                updateDTO.getTitle(),
                updateDTO.getVideoUrl(),
                updateDTO.getMaterialFile(),
                updateDTO.getSectionOrder(),
                updateDTO.getOpenDate()
        );
    }

    @Transactional
    public void deleteSection(Integer sectionId) {
        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 섹션이 존재하지 않습니다."));

        Course course = courseRepository.findById(foundSection.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("부모 코스가 존재하지 않습니다."));

        if (course.getApprovalStatus() == CourseApprovalStatus.APPROVED) {
            throw new IllegalArgumentException("승인된 코스의 섹션은 삭제할 수 없습니다.");
        }

        sectionRepository.deleteById(sectionId);
    }

    public List<SectionDTO> findMySections(Integer memberId, Integer courseId) {

        enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        if (course.getApprovalStatus() != CourseApprovalStatus.APPROVED || !Boolean.TRUE.equals(course.getIsOpen())) {
            throw new IllegalArgumentException("접근할 수 없는 코스입니다.");
        }

        List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);

        return sectionList.stream()
                .map(section -> modelMapper.map(section, SectionDTO.class))
                .collect(Collectors.toList());
    }

    public SectionDTO findMySectionDetail(Integer memberId, Integer courseId, Integer sectionId) {

        enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 섹션이 존재하지 않습니다."));

        if (!foundSection.getCourseId().equals(courseId)) {
            throw new IllegalArgumentException("해당 코스의 섹션이 아닙니다.");
        }

        return modelMapper.map(foundSection, SectionDTO.class);
    }

}