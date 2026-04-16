package com.wanted.projectmodule2lms.domain.submission.controller;

import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionCreateDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionScoreDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionUpdateDTO;
import com.wanted.projectmodule2lms.domain.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ModelAndView findSubmissionsByAssignment(@PathVariable Integer assignmentId, ModelAndView mv) {
        mv.addObject("assignmentId", assignmentId);
        mv.addObject("assignment", assignmentService.findAssignmentById(assignmentId));
        mv.addObject("submissionList", submissionService.findSubmissionsByAssignmentId(assignmentId));
        mv.setViewName("submission/list");
        return mv;
    }

    @GetMapping("/assignments/{assignmentId}/submissions/regist")
    public ModelAndView registPage(@PathVariable Integer assignmentId, ModelAndView mv) {
        mv.addObject("assignmentId", assignmentId);
        mv.addObject("assignment", assignmentService.findAssignmentById(assignmentId));
        mv.setViewName("submission/regist");
        return mv;
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public String registSubmission(@PathVariable Integer assignmentId,
                                   @ModelAttribute SubmissionCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        try {
            submissionService.registSubmission(assignmentId, createDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 제출되었습니다.");
            return "redirect:/assignments/" + assignmentId + "/submissions";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/assignments/" + assignmentId + "/submissions/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 제출 중 오류가 발생했습니다.");
            return "redirect:/assignments/" + assignmentId + "/submissions/regist";
        }
    }

    @GetMapping("/submissions/{submissionId}")
    public ModelAndView findSubmissionById(@PathVariable Integer submissionId, ModelAndView mv) {
        mv.addObject("submission", submissionService.findSubmissionById(submissionId));
        mv.setViewName("submission/detail");
        return mv;
    }

    @GetMapping("/submissions/{submissionId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer submissionId, ModelAndView mv) {
        mv.addObject("submission", submissionService.findSubmissionById(submissionId));
        mv.setViewName("submission/modify");
        return mv;
    }

    @PostMapping("/submissions/{submissionId}/modify")
    public String modifySubmission(@PathVariable Integer submissionId,
                                   @ModelAttribute SubmissionUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        Integer assignmentId = submissionService.findSubmissionById(submissionId).getAssignmentId();

        try {
            submissionService.modifySubmission(submissionId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "제출물이 수정되었습니다.");
            return "redirect:/assignments/" + assignmentId + "/submissions";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/submissions/" + submissionId + "/modify";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "제출물 수정 중 오류가 발생했습니다.");
            return "redirect:/submissions/" + submissionId + "/modify";
        }
    }

    @PostMapping("/submissions/{submissionId}/score")
    public String scoreSubmission(@PathVariable Integer submissionId,
                                  @ModelAttribute SubmissionScoreDTO scoreDTO,
                                  RedirectAttributes rttr) {
        Integer assignmentId = submissionService.findSubmissionById(submissionId).getAssignmentId();

        try {
            submissionService.scoreSubmission(submissionId, scoreDTO);
            rttr.addFlashAttribute("successMessage", "채점 및 피드백이 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/assignments/" + assignmentId + "/submissions";
    }
}