package com.wanted.projectmodule2lms.domain.submission.controller;

import com.wanted.projectmodule2lms.domain.assignment.model.dto.AssignmentDTO;
import com.wanted.projectmodule2lms.domain.assignment.service.AssignmentService;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionCreateDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionScoreDTO;
import com.wanted.projectmodule2lms.domain.submission.model.dto.SubmissionUpdateDTO;
import com.wanted.projectmodule2lms.domain.submission.service.SubmissionService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
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
    public ModelAndView findSubmissionsByCourse(@PathVariable Integer courseId,
                                                @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                                ModelAndView mv) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 제출 목록을 조회할 수 있습니다.");
        }

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignment);
        mv.addObject("submissionList", submissionService.findSubmissionsByAssignmentId(assignment.getAssignmentId()));
        mv.addObject("role", role);
        mv.setViewName("submission/list");
        return mv;
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/regist")
    public ModelAndView registPage(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "STUDENT") String role,
                                   ModelAndView mv) {
        if (!"STUDENT".equals(role)) {
            throw new IllegalArgumentException("학생만 과제를 제출할 수 있습니다.");
        }

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignment);
        mv.addObject("role", role);
        mv.setViewName("submission/regist");
        return mv;
    }

    @PostMapping("/courses/{courseId}/assignment/submissions")
    public String registSubmission(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "STUDENT") String role,
                                   @LoginMemberId Long memberId,
                                   @ModelAttribute SubmissionCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        if (!"STUDENT".equals(role)) {
            throw new IllegalArgumentException("학생만 과제를 제출할 수 있습니다.");
        }

        if (memberId == null) {
            return "redirect:/auth/login";
        }

        try {
            AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
            Integer enrollmentId = submissionService.findEnrollmentIdByMemberAndCourse(Math.toIntExact(memberId), courseId);

            submissionService.registSubmission(
                    assignment.getAssignmentId(),
                    enrollmentId,
                    createDTO,
                    attachmentUpload
            );

            rttr.addFlashAttribute("successMessage", "과제가 제출되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment/submissions/me?role=STUDENT";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/assignment/submissions/regist?role=STUDENT";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "과제 제출 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/assignment/submissions/regist?role=STUDENT";
        }
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/me")
    public ModelAndView findMySubmission(@PathVariable Integer courseId,
                                         @LoginMemberId Long memberId,
                                         @RequestParam(defaultValue = "STUDENT") String role,
                                         ModelAndView mv) {
        if (!"STUDENT".equals(role)) {
            throw new IllegalArgumentException("학생만 본인 제출을 조회할 수 있습니다.");
        }

        if (memberId == null) {
            mv.setViewName("redirect:/auth/login");
            return mv;
        }

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
        Integer enrollmentId = submissionService.findEnrollmentIdByMemberAndCourse(Math.toIntExact(memberId), courseId);
        SubmissionDTO submission = submissionService.findMySubmission(assignment.getAssignmentId(), enrollmentId);

        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("assignment", assignment);
        mv.addObject("submission", submission);
        mv.addObject("role", role);
        mv.setViewName("submission/detail");
        return mv;
    }

    @GetMapping("/submissions/{submissionId}")
    public ModelAndView findSubmissionById(@PathVariable Integer submissionId,
                                           @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                           ModelAndView mv) {
        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        mv.addObject("courseId", courseId);
        mv.addObject("submission", submission);
        mv.addObject("role", role);
        mv.setViewName("submission/detail");
        return mv;
    }

    @GetMapping("/submissions/{submissionId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer submissionId,
                                   @RequestParam(defaultValue = "STUDENT") String role,
                                   ModelAndView mv) {
        if (!"STUDENT".equals(role)) {
            throw new IllegalArgumentException("학생만 제출물을 수정할 수 있습니다.");
        }

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        mv.addObject("courseId", courseId);
        mv.addObject("submission", submission);
        mv.addObject("role", role);
        mv.setViewName("submission/modify");
        return mv;
    }

    @PostMapping("/submissions/{submissionId}/modify")
    public String modifySubmission(@PathVariable Integer submissionId,
                                   @RequestParam(defaultValue = "STUDENT") String role,
                                   @ModelAttribute SubmissionUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        if (!"STUDENT".equals(role)) {
            throw new IllegalArgumentException("학생만 제출물을 수정할 수 있습니다.");
        }

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.modifySubmission(submissionId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "제출물이 수정되었습니다.");
            return "redirect:/courses/" + courseId + "/assignment/submissions/me?role=STUDENT";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/submissions/" + submissionId + "/modify?role=STUDENT";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "제출물 수정 중 오류가 발생했습니다.");
            return "redirect:/submissions/" + submissionId + "/modify?role=STUDENT";
        }
    }

    @PostMapping("/submissions/{submissionId}/score")
    public String scoreSubmission(@PathVariable Integer submissionId,
                                  @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                  @ModelAttribute SubmissionScoreDTO scoreDTO,
                                  RedirectAttributes rttr) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 채점할 수 있습니다.");
        }

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.scoreSubmission(submissionId, scoreDTO);
            rttr.addFlashAttribute("successMessage", "채점 및 피드백이 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/" + courseId + "/assignment/submissions?role=INSTRUCTOR";
    }
}
