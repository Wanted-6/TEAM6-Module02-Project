package com.wanted.projectmodule2lms.domain.section.controller;

import com.wanted.projectmodule2lms.domain.section.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/courses/{courseId}/sections")
public class StudentSectionController {

    private final SectionService sectionService;

    @GetMapping
    public ModelAndView findMySections(@PathVariable Integer courseId,
                                       @RequestParam Integer memberId,
                                       ModelAndView mv) {
        mv.addObject("memberId", memberId);
        mv.addObject("courseId", courseId);
        mv.addObject("sectionList", sectionService.findMySections(memberId, courseId));
        mv.setViewName("student/section/list");
        return mv;
    }

    @GetMapping("/{sectionId}")
    public ModelAndView findMySectionDetail(@PathVariable Integer courseId,
                                            @PathVariable Integer sectionId,
                                            @RequestParam Integer memberId,
                                            ModelAndView mv) {
        mv.addObject("memberId", memberId);
        mv.addObject("courseId", courseId);
        mv.addObject("section", sectionService.findMySectionDetail(memberId, courseId, sectionId));
        mv.setViewName("student/section/detail");
        return mv;
    }
}