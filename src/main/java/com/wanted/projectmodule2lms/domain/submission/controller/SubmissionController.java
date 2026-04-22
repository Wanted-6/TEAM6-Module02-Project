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
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedInstructorException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedStudentAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;

    @GetMapping("/courses/{courseId}/assignment/submissions")
    public String findSubmissionsByCourse(@PathVariable Integer courseId,
                                          @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                          Model model) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new UnauthorizedInstructorException("媛뺤궗留??쒖텧 ?꾪솴??議고쉶?????덉뒿?덈떎.");
        }

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute(
                "submissionList",
                submissionService.findSubmissionsByAssignmentId(courseId, assignment.getAssignmentId())
        );
        model.addAttribute("role", role);
        return "submission/list";
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/regist")
    public String registPage(@PathVariable Integer courseId,
                             @RequestParam(defaultValue = "STUDENT") String role,
                             Model model) {
        if (!"STUDENT".equals(role)) {
            throw new UnauthorizedStudentAccessException("?숈깮留?怨쇱젣瑜??쒖텧?????덉뒿?덈떎.");
        }

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute("role", role);
        return "submission/regist";
    }

    @PostMapping("/courses/{courseId}/assignment/submissions")
    public String registSubmission(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "STUDENT") String role,
                                   @RequestParam Integer sectionId,
                                   @LoginMemberId Long memberId,
                                   @ModelAttribute SubmissionCreateDTO createDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        if (!"STUDENT".equals(role)) {
            throw new UnauthorizedStudentAccessException("?숈깮留?怨쇱젣瑜??쒖텧?????덉뒿?덈떎.");
        }

        requireMemberId(memberId);

        try {
            AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
            Integer enrollmentId = submissionService.findEnrollmentIdByMemberAndCourse(Math.toIntExact(memberId), courseId);

            submissionService.registSubmission(
                    assignment.getAssignmentId(),
                    enrollmentId,
                    createDTO,
                    attachmentUpload
            );

            rttr.addFlashAttribute("successMessage", "怨쇱젣媛 ?쒖텧?섏뿀?듬땲??");
            return "redirect:/student/attendance/" + courseId + "/" + sectionId;

        } catch (ResourceNotFoundException | UnauthorizedStudentAccessException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/attendance/" + courseId + "/" + sectionId;

        } catch (Exception e) {
            log.error("怨쇱젣 ?쒖텧 以??덉쇅 諛쒖깮 - courseId={}, sectionId={}, memberId={}",
                    courseId, sectionId, memberId, e);

            String message = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "怨쇱젣 ?쒖텧 以?泥섎━?????녿뒗 臾몄젣媛 諛쒖깮?덉뒿?덈떎.";

            rttr.addFlashAttribute("errorMessage", message);
            return "redirect:/student/attendance/" + courseId + "/" + sectionId;
        }
    }

    @GetMapping("/courses/{courseId}/assignment/submissions/me")
    public String findMySubmission(@PathVariable Integer courseId,
                                   @LoginMemberId Long memberId,
                                   @RequestParam(defaultValue = "STUDENT") String role,
                                   Model model) {
        if (!"STUDENT".equals(role)) {
            throw new UnauthorizedStudentAccessException("?숈깮留?蹂몄씤 ?쒖텧??議고쉶?????덉뒿?덈떎.");
        }

        requireMemberId(memberId);

        AssignmentDTO assignment = assignmentService.findAssignmentByCourseId(courseId);
        Integer enrollmentId = submissionService.findEnrollmentIdByMemberAndCourse(Math.toIntExact(memberId), courseId);
        SubmissionDTO submission = submissionService.findMySubmission(assignment.getAssignmentId(), enrollmentId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", submission);
        model.addAttribute("role", role);
        return "submission/detail";
    }

    @GetMapping("/submissions/{submissionId}")
    public String findSubmissionById(@PathVariable Integer submissionId,
                                     @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                     Model model) {
        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        model.addAttribute("courseId", courseId);
        model.addAttribute("submission", submission);
        model.addAttribute("role", role);
        return "submission/detail";
    }

    @GetMapping("/submissions/{submissionId}/modify")
    public String modifyPage(@PathVariable Integer submissionId,
                             @RequestParam(defaultValue = "STUDENT") String role,
                             Model model) {
        if (!"STUDENT".equals(role)) {
            throw new UnauthorizedStudentAccessException("?숈깮留??쒖텧臾쇱쓣 ?섏젙?????덉뒿?덈떎.");
        }

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        model.addAttribute("courseId", courseId);
        model.addAttribute("submission", submission);
        model.addAttribute("role", role);
        return "submission/modify";
    }

    @PostMapping("/submissions/{submissionId}/modify")
    public String modifySubmission(@PathVariable Integer submissionId,
                                   @RequestParam(defaultValue = "STUDENT") String role,
                                   @ModelAttribute SubmissionUpdateDTO updateDTO,
                                   @RequestParam(value = "attachmentUpload", required = false) MultipartFile attachmentUpload,
                                   RedirectAttributes rttr) {
        if (!"STUDENT".equals(role)) {
            throw new UnauthorizedStudentAccessException("?숈깮留??쒖텧臾쇱쓣 ?섏젙?????덉뒿?덈떎.");
        }

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.modifySubmission(submissionId, updateDTO, attachmentUpload);
            rttr.addFlashAttribute("successMessage", "?쒖텧臾쇱씠 ?섏젙?섏뿀?듬땲??");
            return "redirect:/courses/" + courseId + "/assignment/submissions/me?role=STUDENT";
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/submissions/" + submissionId + "/modify?role=STUDENT";
        } catch (Exception e) {
            log.error("?쒖텧臾??섏젙 以??덉쇅 諛쒖깮 - submissionId={}, courseId={}", submissionId, courseId, e);
            rttr.addFlashAttribute("errorMessage", "?쒖텧臾??섏젙 以?泥섎━?????녿뒗 臾몄젣媛 諛쒖깮?덉뒿?덈떎.");
            return "redirect:/submissions/" + submissionId + "/modify?role=STUDENT";
        }
    }

    @PostMapping("/submissions/{submissionId}/score")
    public String scoreSubmission(@PathVariable Integer submissionId,
                                  @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                  @ModelAttribute SubmissionScoreDTO scoreDTO,
                                  RedirectAttributes rttr) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new UnauthorizedInstructorException("媛뺤궗留?梨꾩젏?????덉뒿?덈떎.");
        }

        SubmissionDTO submission = submissionService.findSubmissionById(submissionId);
        Integer courseId = assignmentService.findAssignmentById(submission.getAssignmentId()).getCourseId();

        try {
            submissionService.scoreSubmission(submissionId, scoreDTO);
            rttr.addFlashAttribute("successMessage", "梨꾩젏 諛??쇰뱶諛깆씠 ??λ릺?덉뒿?덈떎.");
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/" + courseId + "/assignment/submissions?role=INSTRUCTOR";
    }

    private void requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new LoginRequiredException("濡쒓렇?명븳 ?ъ슜???뺣낫媛 ?꾩슂?⑸땲??");
        }
    }
}
