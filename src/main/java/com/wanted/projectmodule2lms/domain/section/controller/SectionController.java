package com.wanted.projectmodule2lms.domain.section.controller;

import com.wanted.projectmodule2lms.domain.section.model.dto.SectionCreateDTO;
import com.wanted.projectmodule2lms.domain.section.model.dto.SectionUpdateDTO;
import com.wanted.projectmodule2lms.domain.section.model.service.SectionService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @AuditLog
    @GetMapping("/courses/{courseId}/sections")
    public String findSectionsByCourse(@PathVariable Integer courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("sectionList", sectionService.findSectionsByCourseId(courseId));
        return "instructor/section/list";
    }

    @GetMapping("/courses/{courseId}/sections/regist")
    public String registPage(@PathVariable Integer courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "instructor/section/regist";
    }

    @PostMapping("/courses/{courseId}/sections")
    public String registSection(@PathVariable Integer courseId,
                                @ModelAttribute SectionCreateDTO createDTO,
                                @RequestParam(value = "materialUpload", required = false) MultipartFile materialUpload,
                                RedirectAttributes rttr) {
        try {
            sectionService.registSection(courseId, createDTO, materialUpload);
            rttr.addFlashAttribute("successMessage", "섹션이 등록되었습니다.");
            return "redirect:/courses/" + courseId + "/sections";
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/sections/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "섹션 등록 중 처리할 수 없는 문제가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/sections/regist";
        }
    }

    @AuditLog
    @GetMapping("/sections/{sectionId}")
    public String findSectionById(@PathVariable Integer sectionId, Model model) {
        model.addAttribute("section", sectionService.findSectionById(sectionId));
        return "instructor/section/detail";
    }

    @GetMapping("/sections/{sectionId}/modify")
    public String modifyPage(@PathVariable Integer sectionId, Model model) {
        model.addAttribute("section", sectionService.findSectionById(sectionId));
        return "instructor/section/modify";
    }

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
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sections/" + sectionId + "/modify";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "섹션 수정 중 처리할 수 없는 문제가 발생했습니다.");
            return "redirect:/sections/" + sectionId + "/modify";
        }
    }

    @PostMapping("/sections/{sectionId}/delete")
    public String deleteSection(@PathVariable Integer sectionId,
                                RedirectAttributes rttr) {
        Integer courseId = sectionService.findCourseIdBySectionId(sectionId);

        try {
            sectionService.deleteSection(sectionId);
            rttr.addFlashAttribute("successMessage", "섹션이 삭제되었습니다.");
            return "redirect:/courses/" + courseId + "/sections";
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sections/" + sectionId;
        }
    }
}
