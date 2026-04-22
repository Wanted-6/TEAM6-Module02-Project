package com.wanted.projectmodule2lms.domain.assignment.controller;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentCreateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentUpdateDTO;
import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.submission.service.SubmissionService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedInstructorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final SubmissionService submissionService;

    @AuditLog
    @GetMapping("/courses/{courseId}/assignment")
    public String findAssignmentByCourse(@PathVariable Integer courseId,
                                         @RequestParam(defaultValue = "STUDENT") String role,
                                         Model model) {
        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute("role", role);

        if ("INSTRUCTOR".equals(role)) {
            model.addAttribute(
                    "submissionList",
                    submissionService.findSubmissionsByAssignmentId(courseId, assignment.getAssignmentId())
            );
        }

        return "assignment/detail";
    }

    @GetMapping("/courses/{courseId}/assignment/regist")
    public String registPage(@PathVariable Integer courseId,
                             @RequestParam(defaultValue = "INSTRUCTOR") String role,
                             Model model) {
        validateInstructorRole(role, "媛뺤궗留?怨쇱젣瑜??깅줉?????덉뒿?덈떎.");

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        return "assignment/regist";
    }

    @PostMapping("/courses/{courseId}/assignment")
    public String registAssignment(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                   @ModelAttribute AssignmentCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        validateInstructorRole(role, "媛뺤궗留?怨쇱젣瑜??깅줉?????덉뒿?덈떎.");

        try {
            assignmentService.registAssignment(courseId, createDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "怨쇱젣媛 ?깅줉?섏뿀?듬땲??");
            return "redirect:/courses/" + courseId + "/assignment?role=INSTRUCTOR";
        } catch (ResourceNotFoundException | UnauthorizedInstructorException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/regist?role=INSTRUCTOR";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/regist?role=INSTRUCTOR";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "怨쇱젣 ?깅줉 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return "redirect:/courses/" + courseId + "/assignment/regist?role=INSTRUCTOR";
        }
    }

    @GetMapping("/courses/{courseId}/assignment/modify")
    public String modifyPage(@PathVariable Integer courseId,
                             @RequestParam(defaultValue = "INSTRUCTOR") String role,
                             Model model) {
        validateInstructorRole(role, "媛뺤궗留?怨쇱젣瑜??섏젙?????덉뒿?덈떎.");

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignmentService.findAssignmentByCourseId(courseId));
        return "assignment/modify";
    }

    @PostMapping("/courses/{courseId}/assignment/modify")
    public String modifyAssignment(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                   @ModelAttribute AssignmentUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        validateInstructorRole(role, "媛뺤궗留?怨쇱젣瑜??섏젙?????덉뒿?덈떎.");

        try {
            assignmentService.modifyAssignmentByCourseId(courseId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "怨쇱젣媛 ?섏젙?섏뿀?듬땲??");
            return "redirect:/courses/" + courseId + "/assignment?role=INSTRUCTOR";
        } catch (ResourceNotFoundException | UnauthorizedInstructorException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/modify?role=INSTRUCTOR";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/modify?role=INSTRUCTOR";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "怨쇱젣 ?섏젙 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return "redirect:/courses/" + courseId + "/assignment/modify?role=INSTRUCTOR";
        }
    }

    private void validateInstructorRole(String role, String message) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new UnauthorizedInstructorException(message);
        }
    }
}
