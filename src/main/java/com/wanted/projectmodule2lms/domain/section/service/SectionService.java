package com.wanted.projectmodule2lms.domain.section.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionCreateDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionListItemDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionUpdateDTO;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ResourceLoader resourceLoader;

    public List<SectionListItemDTO> findSectionsByCourseId(Integer courseId) {
        return sectionRepository.findSectionListByCourseId(courseId);
    }

    public SectionDTO findSectionById(Integer sectionId) {
        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 섹션이 존재하지 않습니다."));

        return modelMapper.map(foundSection, SectionDTO.class);
    }

    public Integer findCourseIdBySectionId(Integer sectionId) {
        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 섹션이 존재하지 않습니다."));

        return foundSection.getCourseId();
    }

    public List<SectionDTO> findMySectionsForAttendance(Integer memberId, Integer courseId) {
        validateMyCourseAccess(memberId, courseId);

        List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);

        return sectionList.stream()
                .map(section -> modelMapper.map(section, SectionDTO.class))
                .toList();
    }

    @Transactional
    public Integer registSection(Integer courseId,
                                 SectionCreateDTO createDTO,
                                 MultipartFile materialUpload) throws IOException {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("코스가 존재하지 않습니다.");
        }

        if (createDTO.getSectionOrder() == null || createDTO.getSectionOrder() < 1 || createDTO.getSectionOrder() > 8) {
            throw new IllegalArgumentException("섹션 순서는 1부터 8까지만 가능합니다.");
        }

        if (sectionRepository.existsByCourseIdAndSectionOrder(courseId, createDTO.getSectionOrder())) {
            throw new IllegalArgumentException("같은 코스 안에 중복된 섹션 순서가 존재합니다.");
        }

        if (sectionRepository.existsByCourseIdAndOpenDate(courseId, createDTO.getOpenDate())) {
            throw new IllegalArgumentException("같은 코스에는 동일한 날짜의 섹션을 등록할 수 없습니다.");
        }

        if (sectionRepository.countByCourseId(courseId) >= 8) {
            throw new IllegalArgumentException("한 코스의 섹션은 8개까지만 등록할 수 있습니다.");
        }

        String materialPath = saveMaterialFile(materialUpload);

        if (materialPath == null || materialPath.isBlank()) {
            materialPath = createDTO.getMaterialFile();
        }

        Section section = new Section(
                createDTO.getSectionId(),
                courseId,
                createDTO.getTitle(),
                createDTO.getVideoUrl(),
                materialPath,
                createDTO.getSectionOrder(),
                createDTO.getOpenDate()
        );

        sectionRepository.save(section);
        return section.getSectionId();
    }

    @Transactional
    public void modifySection(Integer sectionId,
                              SectionUpdateDTO updateDTO,
                              MultipartFile materialUpload) throws IOException {
        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 섹션이 존재하지 않습니다."));

        if (sectionRepository.existsByCourseIdAndSectionOrderAndSectionIdNot(
                foundSection.getCourseId(),
                updateDTO.getSectionOrder(),
                sectionId
        )) {
            throw new IllegalArgumentException("같은 코스 안에 중복된 섹션 순서가 존재합니다.");
        }

        if (sectionRepository.existsByCourseIdAndOpenDateAndSectionIdNot(
                foundSection.getCourseId(),
                updateDTO.getOpenDate(),
                sectionId
        )) {
            throw new IllegalArgumentException("같은 코스에는 동일한 날짜의 섹션을 등록할 수 없습니다.");
        }

        if (updateDTO.getSectionOrder() < 1 || updateDTO.getSectionOrder() > 8) {
            throw new IllegalArgumentException("섹션 순서는 1부터 8까지만 가능합니다.");
        }

        String materialPath = foundSection.getMaterialFile();
        String newMaterialPath = saveMaterialFile(materialUpload);

        if (newMaterialPath != null && !newMaterialPath.isBlank()) {
            materialPath = newMaterialPath;
        } else if (updateDTO.getMaterialFile() != null && !updateDTO.getMaterialFile().isBlank()) {
            materialPath = updateDTO.getMaterialFile();
        }

        foundSection.changeSectionInfo(
                updateDTO.getTitle(),
                updateDTO.getVideoUrl(),
                materialPath,
                updateDTO.getSectionOrder(),
                updateDTO.getOpenDate()
        );
    }

    @Transactional
    public void deleteSection(Integer sectionId) {
        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 섹션이 존재하지 않습니다."));

        Course course = courseRepository.findById(foundSection.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("코스가 존재하지 않습니다."));

        if (course.getApprovalStatus() == CourseApprovalStatus.APPROVED) {
            throw new IllegalArgumentException("승인된 코스의 섹션은 삭제할 수 없습니다.");
        }

        sectionRepository.deleteById(sectionId);
    }

    public List<SectionListItemDTO> findMySections(Integer memberId, Integer courseId) {
        validateMyCourseAccess(memberId, courseId);
        return sectionRepository.findSectionListByCourseId(courseId);
    }

    public SectionDTO findMySectionDetail(Integer memberId, Integer courseId, Integer sectionId) {
        validateMyCourseAccess(memberId, courseId);

        Section foundSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 섹션이 존재하지 않습니다."));

        if (!foundSection.getCourseId().equals(courseId)) {
            throw new IllegalArgumentException("해당 코스의 섹션이 아닙니다.");
        }

        return modelMapper.map(foundSection, SectionDTO.class);
    }

    private void validateMyCourseAccess(Integer memberId, Integer courseId) {
        enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        if (course.getApprovalStatus() != CourseApprovalStatus.APPROVED || !Boolean.TRUE.equals(course.getIsOpen())) {
            throw new IllegalArgumentException("접근할 수 없는 코스입니다.");
        }
    }

    private String saveMaterialFile(MultipartFile materialUpload) throws IOException {
        if (materialUpload == null || materialUpload.isEmpty()) {
            return null;
        }

        Resource resource = resourceLoader.getResource("classpath:static/files/section");
        String filePath;

        if (!resource.exists()) {
            String root = "src/main/resources/static/files/section";
            File file = new File(root);
            file.mkdirs();
            filePath = file.getAbsolutePath();
        } else {
            filePath = resourceLoader
                    .getResource("classpath:static/files/section")
                    .getFile()
                    .getAbsolutePath();
        }

        String originFileName = materialUpload.getOriginalFilename();
        String ext = "";

        if (originFileName != null && originFileName.contains(".")) {
            ext = originFileName.substring(originFileName.lastIndexOf("."));
        }

        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

        materialUpload.transferTo(new File(filePath + "/" + savedName));

        return "files/section/" + savedName;
    }

    @Transactional
    protected void updateExamDueDate(Integer courseId) {
        Section lastSection = sectionRepository.findByCourseIdAndSectionOrder(courseId, 8)
                .orElse(null);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스가 존재하지 않습니다."));

        if (lastSection == null || lastSection.getOpenDate() == null) {
            course.changeExamDueDate(null);
            return;
        }

        course.changeExamDueDate(lastSection.getOpenDate().plusDays(3));
    }
}
