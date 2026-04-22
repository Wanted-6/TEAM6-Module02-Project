package com.wanted.projectmodule2lms.domain.submission.controller;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionCreateDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionScoreDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionUpdateDTO;
import com.wanted.projectmodule2lms.domain.submission.service.SubmissionService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedInstructorException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedStudentAccessException;
import com.wanted.projectmodule2lms.global.service.CurrentMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final CurrentMemberService currentMemberService;

    @GetMapping("/courses/{courseId}/assignment/submissions")
    public String findSubmissionsByCourse(@PathVariable Integer courseId,
                                          @LoginMemberId Long loginMemberId,
                                          Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 제출 현황을 조회할 수 있습니다.");

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute(
                "submissionList",
                submissionService.findSubmissionsByAssignmentId(courseId, assignment.getAssignmentId())
        );
        model.addAttribute("role", currentRole.name());
        return "submission/list";
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/regist")
    public String registPage(@PathVariable Integer courseId,
                             @LoginMemberId Long loginMemberId,
                             Model model) {
        Integer memberId = requireMemberId(loginMemberId);
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateStudentRole(currentRole, "학생만 과제 제출 페이지에 접근할 수 있습니다.");

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute("role", currentRole.name());
        return "submission/regist";
    }

    @PostMapping("/courses/{courseId}/assignment/submissions")
    public String registSubmission(@PathVariable Integer courseId,
                                   @RequestParam Integer sectionId,
                                   @LoginMemberId Long loginMemberId,
                                   @ModelAttribute SubmissionCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        Integer memberId = requireMemberId(loginMemberId);
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateStudentRole(currentRole, "학생만 과제를 제출할 수 있습니다.");

        try {
            AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
            Integer enrollmentId = submissionService.findEnrollmentIdByMemberAndCourse(memberId, courseId);

            submissionService.registSubmission(
                    assignment.getAssignmentId(),
                    enrollmentId,
                    createDTO,
                    attachmentUpload
            );

            rttr.addFlashAttribute("successMessage", "과제가 제출되었습니다.");
            return "redirect:/student/attendance/" + courseId + "/" + sectionId;

        } catch (ResourceNotFoundException | UnauthorizedStudentAccessException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/attendance/" + courseId + "/" + sectionId;

        } catch (Exception e) {
            log.error("과제 제출 중 오류 발생 - courseId={}, sectionId={}, memberId={}",
                    courseId, sectionId, memberId, e);

            String message = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "과제 제출 중 알 수 없는 오류가 발생했습니다.";

            rttr.addFlashAttribute("errorMessage", message);
            return "redirect:/student/attendance/" + courseId + "/" + sectionId;
        }
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/me")
    public String findMySubmission(@PathVariable Integer courseId,
                                   @LoginMemberId Long loginMemberId,
                                   Model model) {
        Integer memberId = requireMemberId(loginMemberId);
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateStudentRole(currentRole, "학생만 본인 제출물을 조회할 수 있습니다.");

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
        Integer enrollmentId = submissionService.findEnrollmentIdByMemberAndCourse(memberId, courseId);
        SubmissionDTO submission = submissionService.findMySubmission(assignment.getAssignmentId(), enrollmentId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", submission);
        model.addAttribute("role", currentRole.name());
        return "submission/detail";
    }

    @GetMapping("/submissions/{submissionId}")
    public String findSubmissionById(@PathVariable Integer submissionId,
                                     @LoginMemberId Long loginMemberId,
                                     Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 제출 상세를 조회할 수 있습니다.");

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        model.addAttribute("courseId", courseId);
        model.addAttribute("submission", submission);
        model.addAttribute("role", currentRole.name());
        return "submission/detail";
    }

    @GetMapping("/submissions/{submissionId}/modify")
    public String modifyPage(@PathVariable Integer submissionId,
                             @LoginMemberId Long loginMemberId,
                             Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateStudentRole(currentRole, "학생만 제출물을 수정할 수 있습니다.");

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        model.addAttribute("courseId", courseId);
        model.addAttribute("submission", submission);
        model.addAttribute("role", currentRole.name());
        return "submission/modify";
    }

    @PostMapping("/submissions/{submissionId}/modify")
    public String modifySubmission(@PathVariable Integer submissionId,
                                   @LoginMemberId Long loginMemberId,
                                   @ModelAttribute SubmissionUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateStudentRole(currentRole, "학생만 제출물을 수정할 수 있습니다.");

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.modifySubmission(submissionId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "제출물이 수정되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment/submissions/me";
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/submissions/" + submissionId + "/modify";
        } catch (Exception e) {
            log.error("제출물 수정 중 오류 발생 - submissionId={}, courseId={}", submissionId, courseId, e);
            rttr.addFlashAttribute("errorMessage", "제출물 수정 중 알 수 없는 오류가 발생했습니다.");
            return "redirect:/submissions/" + submissionId + "/modify";
        }
    }

    @PostMapping("/submissions/{submissionId}/score")
    public String scoreSubmission(@PathVariable Integer submissionId,
                                  @LoginMemberId Long loginMemberId,
                                  @ModelAttribute SubmissionScoreDTO scoreDTO,
                                  RedirectAttributes rttr) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 채점할 수 있습니다.");

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.scoreSubmission(submissionId, scoreDTO);
            rttr.addFlashAttribute("successMessage", "채점이 성공적으로 처리되었습니다.");
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/" + courseId + "/assignment/submissions";
    }

    private Integer requireMemberId(Long loginMemberId) {
        Integer memberId = currentMemberService.toMemberId(loginMemberId);
        if (memberId == null) {
            throw new LoginRequiredException("로그인이 필요합니다.");
        }
        return memberId;
    }

    private MemberRole requireCurrentRole(Long loginMemberId) {
        Integer memberId = requireMemberId(loginMemberId);
        MemberRole currentRole = currentMemberService.getCurrentMemberRole(memberId);
        if (currentRole == null) {
            throw new LoginRequiredException("로그인이 필요합니다.");
        }
        return currentRole;
    }

    private void validateInstructorRole(MemberRole currentRole, String message) {
        if (currentRole != MemberRole.INSTRUCTOR) {
            throw new UnauthorizedInstructorException(message);
        }
    }

    private void validateStudentRole(MemberRole currentRole, String message) {
        if (currentRole != MemberRole.STUDENT) {
            throw new UnauthorizedStudentAccessException(message);
        }
    }
}
