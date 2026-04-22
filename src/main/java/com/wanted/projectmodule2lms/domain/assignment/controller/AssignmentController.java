package com.wanted.projectmodule2lms.domain.assignment.controller;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentCreateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentUpdateDTO;
import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.submission.service.SubmissionService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedInstructorException;
import com.wanted.projectmodule2lms.global.service.CurrentMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final SubmissionService submissionService;
    private final CurrentMemberService currentMemberService;

    @AuditLog
    @GetMapping("/courses/{courseId}/assignment")
    public String findAssignmentByCourse(@PathVariable Integer courseId,
                                         @LoginMemberId Long loginMemberId,
                                         Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute("role", currentRole.name());

        if (currentRole == MemberRole.INSTRUCTOR) {
            model.addAttribute(
                    "submissionList",
                    submissionService.findSubmissionsByAssignmentId(courseId, assignment.getAssignmentId())
            );
        }

        return "assignment/detail";
    }

    @GetMapping("/courses/{courseId}/assignment/regist")
    public String registPage(@PathVariable Integer courseId,
                             @LoginMemberId Long loginMemberId,
                             Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 과제를 등록할 수 있습니다.");

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("role", currentRole.name());
        return "assignment/regist";
    }

    @PostMapping("/courses/{courseId}/assignment")
    public String registAssignment(@PathVariable Integer courseId,
                                   @LoginMemberId Long loginMemberId,
                                   @ModelAttribute AssignmentCreateDTO createDTO,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 과제를 등록할 수 있습니다.");

        try {
            assignmentService.registAssignment(courseId, createDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 등록되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment";
        } catch (ResourceNotFoundException | UnauthorizedInstructorException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/regist";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 등록 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/regist";
        }
    }

    @GetMapping("/courses/{courseId}/assignment/modify")
    public String modifyPage(@PathVariable Integer courseId,
                             @LoginMemberId Long loginMemberId,
                             Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 과제를 수정할 수 있습니다.");

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignmentService.findAssignmentByCourseId(courseId));
        model.addAttribute("role", currentRole.name());
        return "assignment/modify";
    }

    @PostMapping("/courses/{courseId}/assignment/modify")
    public String modifyAssignment(@PathVariable Integer courseId,
                                   @LoginMemberId Long loginMemberId,
                                   @ModelAttribute AssignmentUpdateDTO updateDTO,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 과제를 수정할 수 있습니다.");

        try {
            assignmentService.modifyAssignmentByCourseId(courseId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 수정되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment";
        } catch (ResourceNotFoundException | UnauthorizedInstructorException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/modify";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/modify";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 수정 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/modify";
        }
    }

    private MemberRole requireCurrentRole(Long loginMemberId) {
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);
        if (currentMemberId == null) {
            throw new LoginRequiredException("로그인이 필요합니다.");
        }

        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);
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
}
