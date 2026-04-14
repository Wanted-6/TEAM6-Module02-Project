package com.wanted.projectmodule2lms.domain.section.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
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
        if (!sectionRepository.existsById(sectionId)) {
            throw new IllegalArgumentException("삭제할 섹션이 존재하지 않습니다.");
        }

        sectionRepository.deleteById(sectionId);
    }
}