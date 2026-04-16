package com.wanted.projectmodule2lms.domain.assignment.service;

import com.wanted.projectmodule2lms.domain.assignment.model.dao.AssignmentRepository;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentCreateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentUpdateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.entity.Assignment;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SectionRepository sectionRepository;
    private final ModelMapper modelMapper;
    private final ResourceLoader resourceLoader;

    public List<AssignmentDTO> findAssignmentsBySectionId(Integer sectionId) {
        List<Assignment> assignmentList = assignmentRepository.findBySectionIdOrderByDueDateAsc(sectionId);

        return assignmentList.stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDTO.class))
                .collect(Collectors.toList());
    }

    public AssignmentDTO findAssignmentById(Integer assignmentId) {
        Assignment foundAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 과제가 존재하지 않습니다."));

        return modelMapper.map(foundAssignment, AssignmentDTO.class);
    }

    @Transactional
    public Integer registAssignment(Integer sectionId,
                                    AssignmentCreateDTO createDTO,
                                    MultipartFile attachmentUpload) throws IOException {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("섹션이 존재하지 않습니다."));

        String attachmentPath = saveAttachmentFile(attachmentUpload);

        if (attachmentPath == null || attachmentPath.isBlank()) {
            attachmentPath = createDTO.getAttachmentFile();
        }

        Assignment assignment = new Assignment(
                section.getSectionId(),
                createDTO.getTitle(),
                createDTO.getDescription(),
                attachmentPath,
                createDTO.getDueDate()
        );

        assignmentRepository.save(assignment);
        return assignment.getAssignmentId();
    }

    @Transactional
    public void modifyAssignment(Integer assignmentId,
                                 AssignmentUpdateDTO updateDTO,
                                 MultipartFile attachmentUpload) throws IOException {

        Assignment foundAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 과제가 존재하지 않습니다."));

        String attachmentPath = foundAssignment.getAttachmentFile();
        String newAttachmentPath = saveAttachmentFile(attachmentUpload);

        if (newAttachmentPath != null && !newAttachmentPath.isBlank()) {
            attachmentPath = newAttachmentPath;
        } else if (updateDTO.getAttachmentFile() != null && !updateDTO.getAttachmentFile().isBlank()) {
            attachmentPath = updateDTO.getAttachmentFile();
        }

        foundAssignment.changeAssignmentInfo(
                updateDTO.getTitle(),
                updateDTO.getDescription(),
                attachmentPath,
                updateDTO.getDueDate()
        );
    }

    private String saveAttachmentFile(MultipartFile attachmentUpload) throws IOException {
        if (attachmentUpload == null || attachmentUpload.isEmpty()) {
            return null;
        }

        Resource resource = resourceLoader.getResource("classpath:static/files/assignment");
        String filePath;

        if (!resource.exists()) {
            String root = "src/main/resources/static/files/assignment";
            File file = new File(root);
            file.mkdirs();
            filePath = file.getAbsolutePath();
        } else {
            filePath = resourceLoader
                    .getResource("classpath:static/files/assignment")
                    .getFile()
                    .getAbsolutePath();
        }

        String originFileName = attachmentUpload.getOriginalFilename();
        String ext = "";

        if (originFileName != null && originFileName.contains(".")) {
            ext = originFileName.substring(originFileName.lastIndexOf("."));
        }

        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

        attachmentUpload.transferTo(new File(filePath + "/" + savedName));

        return "static/files/assignment/" + savedName;
    }
}