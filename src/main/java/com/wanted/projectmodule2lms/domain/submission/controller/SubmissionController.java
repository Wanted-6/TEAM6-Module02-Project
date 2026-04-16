package com.wanted.projectmodule2lms.domain.submission.controller;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionCreateDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionScoreDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionUpdateDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionDTO;
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
    private final CourseService courseService;

    @GetMapping("/courses/{courseId}/assignment/submissions")
    public ModelAndView findSubmissionsByCourse(@PathVariable Integer courseId, ModelAndView mv) {
        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignment);
        mv.addObject("submissionList", submissionService.findSubmissionsByAssignmentId(assignment.getAssignmentId()));
        mv.setViewName("submission/list");
        return mv;
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/regist")
    public ModelAndView registPage(@PathVariable Integer courseId, ModelAndView mv) {
        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignment);
        mv.setViewName("submission/regist");
        return mv;
    }

    @PostMapping("/courses/{courseId}/assignment/submissions")
    public String registSubmission(@PathVariable Integer courseId,
                                   @ModelAttribute SubmissionCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        try {
            AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
            submissionService.registSubmission(assignment.getAssignmentId(), createDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "과제가 제출되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment/submissions/me?enrollmentId=" + createDTO.getEnrollmentId();
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/submissions/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 제출 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/submissions/regist";
        }
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/me")
    public ModelAndView findMySubmission(@PathVariable Integer courseId,
                                         @RequestParam Integer enrollmentId,
                                         ModelAndView mv) {
        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
        SubmissionDTO submission = submissionService.findMySubmission(assignment.getAssignmentId(), enrollmentId);

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignment);
        mv.addObject("submission", submission);
        mv.setViewName("submission/detail");
        return mv;
    }

    @GetMapping("/submissions/{submissionId}")
    public ModelAndView findSubmissionById(@PathVariable Integer submissionId, ModelAndView mv) {
        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        mv.addObject("courseId", courseId);
        mv.addObject("submission", submission);
        mv.setViewName("submission/detail");
        return mv;
    }

    @GetMapping("/submissions/{submissionId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer submissionId, ModelAndView mv) {
        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        mv.addObject("courseId", courseId);
        mv.addObject("submission", submission);
        mv.setViewName("submission/modify");
        return mv;
    }

    @PostMapping("/submissions/{submissionId}/modify")
    public String modifySubmission(@PathVariable Integer submissionId,
                                   @ModelAttribute SubmissionUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.modifySubmission(submissionId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "제출물이 수정되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment/submissions/me?enrollmentId=" + submission.getEnrollmentId();
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
        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.scoreSubmission(submissionId, scoreDTO);
            rttr.addFlashAttribute("successMessage", "채점 및 피드백이 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/" + courseId + "/assignment/submissions";
    }
}