package com.wanted.projectmodule2lms.domain.section.controller;

import com.wanted.projectmodule2lms.domain.section.model.dto.SectionCreateDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionUpdateDTO;
import com.wanted.projectmodule2lms.domain.section.service.SectionService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    /* 특정 코스의 섹션 목록 조회 */
    @AuditLog
    @GetMapping("/courses/{courseId}/sections")
    public ModelAndView findSectionsByCourse(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("sectionList", sectionService.findSectionsByCourseId(courseId));
        mv.setViewName("instructor/section/list");
        return mv;
    }

    /* 섹션 등록 페이지 */
    @GetMapping("/courses/{courseId}/sections/regist")
    public ModelAndView registPage(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.setViewName("instructor/section/regist");
        return mv;
    }

    /* 섹션 등록 */
    @PostMapping("/courses/{courseId}/sections")
    public String registSection(@PathVariable Integer courseId,
                                @ModelAttribute SectionCreateDTO createDTO,
                                @RequestParam(value = "materialUpload", required = false) MultipartFile materialUpload,
                                RedirectAttributes rttr) {
        try {
            sectionService.registSection(courseId, createDTO, materialUpload);
            rttr.addFlashAttribute("successMessage", "섹션이 등록되었습니다.");
            return "redirect:/courses/" + courseId + "/sections";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/sections/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "섹션 등록 중 처리할 수 없는 문제가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/sections/regist";
        }
    }

    /* 섹션 상세 조회 */
    @AuditLog
    @GetMapping("/sections/{sectionId}")
    public ModelAndView findSectionById(@PathVariable Integer sectionId, ModelAndView mv) {
        mv.addObject("section", sectionService.findSectionById(sectionId));
        mv.setViewName("instructor/section/detail");
        return mv;
    }

    /* 섹션 수정 페이지 */
    @GetMapping("/sections/{sectionId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer sectionId, ModelAndView mv) {
        mv.addObject("section", sectionService.findSectionById(sectionId));
        mv.setViewName("instructor/section/modify");
        return mv;
    }

    /* 섹션 수정 */
    @PostMapping("/sections/{sectionId}/modify")
    public String modifySection(@PathVariable Integer sectionId,
                                @ModelAttribute SectionUpdateDTO updateDTO,
                                @RequestParam(value = "materialUpload", required = false) MultipartFile materialUpload,
                                RedirectAttributes rttr) {
        Integer courseId = sectionService.findCourseIdBySectionId(sectionId);

        try {
            sectionService.modifySection(sectionId, updateDTO, materialUpload);
            rttr.addFlashAttribute("successMessage", "섹션이 수정되었습니다.");
            return "redirect:/courses/" + courseId + "/sections";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sections/" + sectionId + "/modify";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "섹션 수정 중 처리할 수 없는 문제가 발생했습니다.");
            return "redirect:/sections/" + sectionId + "/modify";
        }
    }

    /* 섹션 삭제 */
    @PostMapping("/sections/{sectionId}/delete")
    public String deleteSection(@PathVariable Integer sectionId,
                                RedirectAttributes rttr) {
        Integer courseId = sectionService.findCourseIdBySectionId(sectionId);

        try {
            sectionService.deleteSection(sectionId);
            rttr.addFlashAttribute("successMessage", "섹션이 삭제되었습니다.");
            return "redirect:/courses/" + courseId + "/sections";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sections/" + sectionId;
        }
    }
}
