package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.model.dto.CourseCreateDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseUpdateDTO;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedInstructorException;
import com.wanted.projectmodule2lms.global.service.CurrentMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final CurrentMemberService currentMemberService;

    @AuditLog
    @GetMapping
    public String findAllCourses(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String category,
                                 @LoginMemberId Long loginMemberId,
                                 Principal principal,
                                 Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 코스 목록을 조회할 수 있습니다.");

        String loginId = principal.getName();

        model.addAttribute("courseList", courseService.findMyInstructorCourses(loginId, keyword, category));
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("role", currentRole.name());
        return "instructor/course/list";
    }

    @AuditLog
    @GetMapping("/{courseId}")
    public String findCourseById(@PathVariable Integer courseId,
                                 @LoginMemberId Long loginMemberId,
                                 Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 코스 상세를 조회할 수 있습니다.");

        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("role", currentRole.name());
        model.addAttribute("hasAssignment", courseService.hasAssignmentByCourseId(courseId));
        return "instructor/course/detail";
    }

    @GetMapping("/regist")
    public String registPage(@LoginMemberId Long loginMemberId, Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 코스를 등록할 수 있습니다.");
        model.addAttribute("role", currentRole.name());
        return "instructor/course/regist";
    }

    @PostMapping("/regist")
    public String registCourse(@LoginMemberId Long loginMemberId,
                               @ModelAttribute CourseCreateDTO createDTO,
                               @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                               RedirectAttributes rttr,
                               Principal principal) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 코스를 등록할 수 있습니다.");

        try {
            createDTO.setInstructorLoginId(principal.getName());

            Integer courseId = courseService.registCourse(createDTO, thumbnailFile);
            rttr.addFlashAttribute("successMessage", "코스가 등록되었습니다.");
            return "redirect:/courses/" + courseId;
        } catch (ResourceNotFoundException | UnauthorizedInstructorException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/regist";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "코스 등록 중 오류가 발생했습니다.");
            return "redirect:/courses/regist";
        }
    }

    @GetMapping("/{courseId}/modify")
    public String modifyPage(@PathVariable Integer courseId,
                             @LoginMemberId Long loginMemberId,
                             Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 코스를 수정할 수 있습니다.");

        model.addAttribute("course", courseService.findCourseById(courseId));
        model.addAttribute("role", currentRole.name());
        return "instructor/course/modify";
    }

    @PostMapping("/{courseId}/modify")
    public String modifyCourse(@PathVariable Integer courseId,
                               @LoginMemberId Long loginMemberId,
                               @ModelAttribute CourseUpdateDTO updateDTO,
                               @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                               RedirectAttributes rttr,
                               Principal principal) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 코스를 수정할 수 있습니다.");

        try {
            courseService.modifyCourse(courseId, principal.getName(), updateDTO, thumbnailFile);
            rttr.addFlashAttribute("successMessage", "코스가 수정되었습니다.");
            return "redirect:/courses/" + courseId;
        } catch (ResourceNotFoundException | UnauthorizedInstructorException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/modify";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "코스 수정 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/modify";
        }
    }

    @AuditLog
    @GetMapping("/{courseId}/students")
    public String findCourseStudents(@PathVariable Integer courseId,
                                     @LoginMemberId Long loginMemberId,
                                     Model model) {
        MemberRole currentRole = requireCurrentRole(loginMemberId);
        validateInstructorRole(currentRole, "강사만 수강생 목록을 조회할 수 있습니다.");

        model.addAttribute("courseId", courseId);
        model.addAttribute("studentList", courseService.findStudentsByCourseId(courseId));
        model.addAttribute("role", currentRole.name());
        return "instructor/course/students";
    }

    private MemberRole requireCurrentRole(Long loginMemberId) {
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);
        if (currentMemberId == null) {
            throw new LoginRequiredException("로그인이 필요합니다.");
        }

        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);
        if (currentRole == null) {
            throw new LoginRequiredException("로그인이 필요합니다.");
        }
        return currentRole;
    }

    private void validateInstructorRole(MemberRole currentRole, String message) {
        if (currentRole != MemberRole.INSTRUCTOR) {
            throw new UnauthorizedInstructorException(message);
        }
    }
}
