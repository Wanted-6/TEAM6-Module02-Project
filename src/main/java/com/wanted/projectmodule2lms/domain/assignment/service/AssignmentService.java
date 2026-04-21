package com.wanted.projectmodule2lms.domain.assignment.service;

import com.wanted.projectmodule2lms.domain.assignment.model.dao.AssignmentRepository;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentCreateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentUpdateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.entity.Assignment;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;
    private final ResourceLoader resourceLoader;

    public AssignmentDTO findAssignmentByCourseId(Integer courseId) {
        Assignment foundAssignment = assignmentRepository.findByCourseId(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스에 등록된 과제가 없습니다."));

        return modelMapper.map(foundAssignment, AssignmentDTO.class);
    }

    public AssignmentDTO findAssignmentById(Integer assignmentId) {
        Assignment foundAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 과제가 존재하지 않습니다."));

        return modelMapper.map(foundAssignment, AssignmentDTO.class);
    }

    public boolean hasAssignmentByCourseId(Integer courseId) {
        return assignmentRepository.existsByCourseId(courseId);
    }

    @Transactional
    public Integer registAssignment(Integer courseId,
                                    AssignmentCreateDTO createDTO,
                                    MultipartFile attachmentUpload) throws IOException {

        courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스가 존재하지 않습니다."));

        if (assignmentRepository.findByCourseId(courseId).isPresent()) {
            throw new IllegalArgumentException("해당 코스에는 이미 과제가 등록되어 있습니다.");
        }

        String attachmentPath = saveAttachmentFile(attachmentUpload);

        if (attachmentPath == null || attachmentPath.isBlank()) {
            attachmentPath = createDTO.getAttachmentFile();
        }

        Assignment assignment = new Assignment(
                courseId,
                createDTO.getTitle(),
                createDTO.getDescription(),
                attachmentPath,
                createDTO.getDueDate()
        );

        assignmentRepository.save(assignment);
        return assignment.getAssignmentId();
    }

    @Transactional
    public void modifyAssignmentByCourseId(Integer courseId,
                                           AssignmentUpdateDTO updateDTO,
                                           MultipartFile attachmentUpload) throws IOException {

        Assignment foundAssignment = assignmentRepository.findByCourseId(courseId)
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

        return "files/assignment/" + savedName;
    }
}
