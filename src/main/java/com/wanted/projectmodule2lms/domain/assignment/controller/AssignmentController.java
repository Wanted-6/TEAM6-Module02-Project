package com.wanted.projectmodule2lms.domain.assignment.controller;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentCreateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentUpdateDTO;
import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;

    @GetMapping("/courses/{courseId}/assignment")
    public ModelAndView findAssignmentByCourse(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignmentService.findAssignmentByCourseId(courseId));
        mv.setViewName("assignment/detail");
        return mv;
    }

    @GetMapping("/courses/{courseId}/assignment/regist")
    public ModelAndView registPage(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.setViewName("assignment/regist");
        return mv;
    }

    @PostMapping("/courses/{courseId}/assignment")
    public String registAssignment(@PathVariable Integer courseId,
                                   @ModelAttribute AssignmentCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        try {
            assignmentService.registAssignment(courseId, createDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 등록되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 등록 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/regist";
        }
    }

    @GetMapping("/courses/{courseId}/assignment/modify")
    public ModelAndView modifyPage(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignmentService.findAssignmentByCourseId(courseId));
        mv.setViewName("assignment/modify");
        return mv;
    }

    @PostMapping("/courses/{courseId}/assignment/modify")
    public String modifyAssignment(@PathVariable Integer courseId,
                                   @ModelAttribute AssignmentUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        try {
            assignmentService.modifyAssignmentByCourseId(courseId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 수정되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/modify";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 수정 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/modify";
        }
    }
}