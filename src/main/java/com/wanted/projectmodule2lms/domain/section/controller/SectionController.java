package com.wanted.projectmodule2lms.domain.section.controller;

import com.wanted.projectmodule2lms.domain.section.model.dto.SectionCreateDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionUpdateDTO;
import com.wanted.projectmodule2lms.domain.section.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping("/courses/{courseId}/sections")
    public ModelAndView findSectionsByCourse(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("sectionList", sectionService.findSectionsByCourseId(courseId));
        mv.setViewName("instructor/section/list");
        return mv;
    }

    @GetMapping("/courses/{courseId}/sections/regist")
    public ModelAndView registPage(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.setViewName("instructor/section/regist");
        return mv;
    }

    @PostMapping("/courses/{courseId}/sections")
    public String registSection(@PathVariable Integer courseId,
                                @ModelAttribute SectionCreateDTO createDTO,
                                RedirectAttributes rttr) {
        sectionService.registSection(courseId, createDTO);
        rttr.addFlashAttribute("successMessage", "섹션이 등록되었습니다.");
        return "redirect:/courses/" + courseId + "/sections";
    }

    @GetMapping("/sections/{sectionId}")
    public ModelAndView findSectionById(@PathVariable Integer sectionId, ModelAndView mv) {
        mv.addObject("section", sectionService.findSectionById(sectionId));
        mv.setViewName("instructor/section/detail");
        return mv;
    }

    @GetMapping("/sections/{sectionId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer sectionId, ModelAndView mv) {
        mv.addObject("section", sectionService.findSectionById(sectionId));
        mv.setViewName("instructor/section/modify");
        return mv;
    }

    @PostMapping("/sections/{sectionId}/modify")
    public String modifySection(@PathVariable Integer sectionId,
                                @ModelAttribute SectionUpdateDTO updateDTO,
                                RedirectAttributes rttr) {
        Integer courseId = sectionService.findSectionById(sectionId).getCourseId();
        sectionService.modifySection(sectionId, updateDTO);
        rttr.addFlashAttribute("successMessage", "섹션이 수정되었습니다.");
        return "redirect:/courses/" + courseId + "/sections";
    }

    @PostMapping("/sections/{sectionId}/delete")
    public String deleteSection(@PathVariable Integer sectionId,
                                RedirectAttributes rttr) {
        Integer courseId = sectionService.findSectionById(sectionId).getCourseId();
        sectionService.deleteSection(sectionId);
        rttr.addFlashAttribute("successMessage", "섹션이 삭제되었습니다.");
        return "redirect:/courses/" + courseId + "/sections";
    }
}