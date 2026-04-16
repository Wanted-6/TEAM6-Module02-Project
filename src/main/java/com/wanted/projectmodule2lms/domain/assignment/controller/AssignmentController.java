package com.wanted.projectmodule2lms.domain.assignment.controller;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentCreateDTO;
import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentUpdateDTO;
import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.section.service.SectionService;
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
    private final SectionService sectionService;

    @GetMapping("/sections/{sectionId}/assignments")
    public ModelAndView findAssignmentsBySection(@PathVariable Integer sectionId, ModelAndView mv) {
        mv.addObject("sectionId", sectionId);
        mv.addObject("section", sectionService.findSectionById(sectionId));
        mv.addObject("assignmentList", assignmentService.findAssignmentsBySectionId(sectionId));
        mv.setViewName("assignment/list");
        return mv;
    }

    @GetMapping("/sections/{sectionId}/assignments/regist")
    public ModelAndView registPage(@PathVariable Integer sectionId, ModelAndView mv) {
        mv.addObject("sectionId", sectionId);
        mv.addObject("section", sectionService.findSectionById(sectionId));
        mv.setViewName("assignment/regist");
        return mv;
    }

    @PostMapping("/sections/{sectionId}/assignments")
    public String registAssignment(@PathVariable Integer sectionId,
                                   @ModelAttribute AssignmentCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        try {
            assignmentService.registAssignment(sectionId, createDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 등록되었습니다.");
            return "redirect:/sections/" + sectionId + "/assignments";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sections/" + sectionId + "/assignments/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 등록 중 오류가 발생했습니다.");
            return "redirect:/sections/" + sectionId + "/assignments/regist";
        }
    }

    @GetMapping("/assignments/{assignmentId}")
    public ModelAndView findAssignmentById(@PathVariable Integer assignmentId, ModelAndView mv) {
        mv.addObject("assignment", assignmentService.findAssignmentById(assignmentId));
        mv.setViewName("assignment/detail");
        return mv;
    }

    @GetMapping("/assignments/{assignmentId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer assignmentId, ModelAndView mv) {
        mv.addObject("assignment", assignmentService.findAssignmentById(assignmentId));
        mv.setViewName("assignment/modify");
        return mv;
    }

    @PostMapping("/assignments/{assignmentId}/modify")
    public String modifyAssignment(@PathVariable Integer assignmentId,
                                   @ModelAttribute AssignmentUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        Integer sectionId = assignmentService.findAssignmentById(assignmentId).getSectionId();

        try {
            assignmentService.modifyAssignment(assignmentId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 수정되었습니다.");
            return "redirect:/sections/" + sectionId + "/assignments";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/assignments/" + assignmentId + "/modify";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 수정 중 오류가 발생했습니다.");
            return "redirect:/assignments/" + assignmentId + "/modify";
        }
    }
}