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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
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
    public ModelAndView findAssignmentByCourse(@PathVariable Integer courseId,
                                               @RequestParam(defaultValue = "STUDENT") String role,
                                               ModelAndView mv) {
        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignment);
        mv.addObject("role", role);

        if ("INSTRUCTOR".equals(role)) {
            mv.addObject(
                    "submissionList",
                    submissionService.findSubmissionsByAssignmentId(courseId, assignment.getAssignmentId())
            );
        }

        mv.setViewName("assignment/detail");
        return mv;
    }

    @GetMapping("/courses/{courseId}/assignment/regist")
    public ModelAndView registPage(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                   ModelAndView mv) {
        validateInstructorRole(role, "강사만 과제를 등록할 수 있습니다.");

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.setViewName("assignment/regist");
        return mv;
    }

    @PostMapping("/courses/{courseId}/assignment")
    public String registAssignment(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                   @ModelAttribute AssignmentCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        validateInstructorRole(role, "강사만 과제를 등록할 수 있습니다.");

        try {
            assignmentService.registAssignment(courseId, createDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 등록되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment?role=INSTRUCTOR";
        } catch (ResourceNotFoundException | UnauthorizedInstructorException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/regist?role=INSTRUCTOR";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/regist?role=INSTRUCTOR";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 등록 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/regist?role=INSTRUCTOR";
        }
    }

    @GetMapping("/courses/{courseId}/assignment/modify")
    public ModelAndView modifyPage(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                   ModelAndView mv) {
        validateInstructorRole(role, "강사만 과제를 수정할 수 있습니다.");

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignmentService.findAssignmentByCourseId(courseId));
        mv.setViewName("assignment/modify");
        return mv;
    }

    @PostMapping("/courses/{courseId}/assignment/modify")
    public String modifyAssignment(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                   @ModelAttribute AssignmentUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        validateInstructorRole(role, "강사만 과제를 수정할 수 있습니다.");

        try {
            assignmentService.modifyAssignmentByCourseId(courseId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 수정되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment?role=INSTRUCTOR";
        } catch (ResourceNotFoundException | UnauthorizedInstructorException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/modify?role=INSTRUCTOR";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/modify?role=INSTRUCTOR";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 수정 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/modify?role=INSTRUCTOR";
        }
    }

    private void validateInstructorRole(String role, String message) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new UnauthorizedInstructorException(message);
        }
    }
}