package com.wanted.projectmodule2lms.domain.submission.service;

import com.wanted.projectmodule2lms.domain.assignment.model.dao.AssignmentRepository;
import com.wanted.projectmodule2lms.domain.assignment.model.entity.Assignment;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseStudentDTO;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.submission.model.dao.SubmissionRepository;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionCreateDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionListDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionScoreDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionUpdateDTO;
import com.wanted.projectmodule2lms.domain.submission.model.entity.Submission;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final CourseService courseService;
    private final ModelMapper modelMapper;
    private final ResourceLoader resourceLoader;

    public List<SubmissionListDTO> findSubmissionsByAssignmentId(Integer courseId, Integer assignmentId) {
        List<CourseStudentDTO> studentList = courseService.findStudentsByCourseId(courseId);
        List<SubmissionListDTO> result = new ArrayList<>();

        for (CourseStudentDTO student : studentList) {
            Optional<Submission> submissionOpt =
                    submissionRepository.findByAssignmentIdAndEnrollmentId(assignmentId, student.getEnrollmentId());

            if (submissionOpt.isPresent()) {
                Submission submission = submissionOpt.get();

                result.add(new SubmissionListDTO(
                        student.getEnrollmentId(),
                        student.getLoginId(),
                        student.getName(),
                        "제출 완료",
                        submission.getSubmittedAt(),
                        submission.getSubmissionId()
                ));
            } else {
                result.add(new SubmissionListDTO(
                        student.getEnrollmentId(),
                        student.getLoginId(),
                        student.getName(),
                        "미제출",
                        null,
                        null
                ));
            }
        }

        return result;
    }

    public SubmissionDTO findSubmissionById(Integer submissionId) {
        Submission foundSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 제출물이 존재하지 않습니다."));

        return convertToSubmissionDTO(foundSubmission);
    }

    public SubmissionDTO findMySubmission(Integer assignmentId, Integer enrollmentId) {
        Submission foundSubmission = submissionRepository.findByAssignmentIdAndEnrollmentId(assignmentId, enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("제출한 과제가 없습니다."));

        return convertToSubmissionDTO(foundSubmission);
    }

    private SubmissionDTO convertToSubmissionDTO(Submission submission) {
        SubmissionDTO dto = modelMapper.map(submission, SubmissionDTO.class);

        Enrollment enrollment = enrollmentRepository.findById(submission.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("수강 정보가 존재하지 않습니다."));

        Member member = memberRepository.findById(enrollment.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 존재하지 않습니다."));

        dto.setStudentName(member.getName());
        dto.setStudentLoginId(member.getLoginId());

        return dto;
    }

    public Integer findEnrollmentIdByMemberAndCourse(Integer memberId, Integer courseId) {
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스의 수강 정보가 없습니다."));

        return enrollment.getEnrollmentId();
    }

    @Transactional
    public Integer registSubmission(Integer assignmentId,
                                    Integer enrollmentId,
                                    SubmissionCreateDTO createDTO,
                                    MultipartFile attachmentUpload) throws IOException {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("과제가 존재하지 않습니다."));

        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new IllegalArgumentException("마감일이 지나 제출할 수 없습니다.");
        }

        enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 정보가 존재하지 않습니다."));

        if (submissionRepository.findByAssignmentIdAndEnrollmentId(assignmentId, enrollmentId).isPresent()) {
            throw new IllegalArgumentException("이미 제출한 과제입니다.");
        }

        String attachmentPath = saveAttachmentFile(attachmentUpload);

        if (attachmentPath == null || attachmentPath.isBlank()) {
            attachmentPath = createDTO.getAttachmentFile();
        }

        Submission submission = new Submission(
                assignment.getAssignmentId(),
                enrollmentId,
                createDTO.getContent(),
                attachmentPath,
                LocalDateTime.now()
        );

        submissionRepository.save(submission);
        return submission.getSubmissionId();
    }

    @Transactional
    public void modifySubmission(Integer submissionId,
                                 SubmissionUpdateDTO updateDTO,
                                 MultipartFile attachmentUpload) throws IOException {

        Submission foundSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 제출물이 존재하지 않습니다."));

        Assignment assignment = assignmentRepository.findById(foundSubmission.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("과제가 존재하지 않습니다."));

        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new IllegalArgumentException("마감일이 지나 제출물을 수정할 수 없습니다.");
        }

        String attachmentPath = foundSubmission.getAttachmentFile();
        String newAttachmentPath = saveAttachmentFile(attachmentUpload);

        if (newAttachmentPath != null && !newAttachmentPath.isBlank()) {
            attachmentPath = newAttachmentPath;
        } else if (updateDTO.getAttachmentFile() != null && !updateDTO.getAttachmentFile().isBlank()) {
            attachmentPath = updateDTO.getAttachmentFile();
        }

        foundSubmission.changeSubmission(
                updateDTO.getContent(),
                attachmentPath
        );
    }

    @Transactional
    public void scoreSubmission(Integer submissionId, SubmissionScoreDTO scoreDTO) {
        Submission foundSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("채점할 제출물이 존재하지 않습니다."));

        foundSubmission.changeScoreAndFeedback(
                scoreDTO.getScore(),
                scoreDTO.getFeedback()
        );
    }

    private String saveAttachmentFile(MultipartFile attachmentUpload) throws IOException {
        if (attachmentUpload == null || attachmentUpload.isEmpty()) {
            return null;
        }

        Resource resource = resourceLoader.getResource("classpath:static/files/submission");
        String filePath;

        if (!resource.exists()) {
            String root = "src/main/resources/static/files/submission";
            File file = new File(root);
            file.mkdirs();
            filePath = file.getAbsolutePath();
        } else {
            filePath = resourceLoader
                    .getResource("classpath:static/files/submission")
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

        return "files/submission/" + savedName;
    }
}
