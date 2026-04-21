package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.model.dto.CourseCreateDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseUpdateDTO;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @AuditLog
    @GetMapping
    public ModelAndView findAllCourses(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                       Principal principal,
                                       ModelAndView mv) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 코스 목록을 조회할 수 있습니다.");
        }

        String loginId = principal.getName();

        mv.addObject("courseList", courseService.findMyInstructorCourses(loginId, keyword, category));
        mv.addObject("keyword", keyword);
        mv.addObject("category", category);
        mv.addObject("role", role);
        mv.setViewName("instructor/course/list");
        return mv;
    }

    @AuditLog
    @GetMapping("/{courseId}")
    public ModelAndView findCourseById(@PathVariable Integer courseId,
                                       @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                       ModelAndView mv) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 코스 상세를 조회할 수 있습니다.");
        }

        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("role", role);
        mv.addObject("hasAssignment", courseService.hasAssignmentByCourseId(courseId));
        mv.setViewName("instructor/course/detail");
        return mv;
    }

    @GetMapping("/regist")
    public String registPage(@RequestParam(defaultValue = "INSTRUCTOR") String role) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 코스를 등록할 수 있습니다.");
        }
        return "instructor/course/regist";
    }

    @PostMapping("/regist")
    public String registCourse(@RequestParam(defaultValue = "INSTRUCTOR") String role,
                               @ModelAttribute CourseCreateDTO createDTO,
                               @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                               RedirectAttributes rttr,
                               Principal principal) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 코스를 등록할 수 있습니다.");
        }

        try {
            createDTO.setInstructorLoginId(principal.getName());

            Integer courseId = courseService.registCourse(createDTO, thumbnailFile);
            rttr.addFlashAttribute("successMessage", "코스가 등록되었습니다.");
            return "redirect:/courses/" + courseId + "?role=INSTRUCTOR";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/regist?role=INSTRUCTOR";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "코스 등록 중 오류가 발생했습니다.");
            return "redirect:/courses/regist?role=INSTRUCTOR";
        }
    }

    @GetMapping("/{courseId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer courseId,
                                   @RequestParam(defaultValue = "INSTRUCTOR") String role,
                                   ModelAndView mv) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 코스를 수정할 수 있습니다.");
        }

        mv.addObject("course", courseService.findCourseById(courseId));
        mv.addObject("role", role);
        mv.setViewName("instructor/course/modify");
        return mv;
    }

    @PostMapping("/{courseId}/modify")
    public String modifyCourse(@PathVariable Integer courseId,
                               @RequestParam(defaultValue = "INSTRUCTOR") String role,
                               @ModelAttribute CourseUpdateDTO updateDTO,
                               @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                               RedirectAttributes rttr,
                               Principal principal) {
        if (!"INSTRUCTOR".equals(role)) {
            throw new IllegalArgumentException("강사만 코스를 수정할 수 있습니다.");
        }

        try {
            courseService.modifyCourse(courseId, principal.getName(), updateDTO, thumbnailFile);
            rttr.addFlashAttribute("successMessage", "코스가 수정되었습니다.");
            return "redirect:/courses/" + courseId + "?role=INSTRUCTOR";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + courseId + "/modify?role=INSTRUCTOR";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "코스 수정 중 오류가 발생했습니다.");
            return "redirect:/courses/" + courseId + "/modify?role=INSTRUCTOR";
        }
    }

    @AuditLog
    @GetMapping("/{courseId}/students")
    public ModelAndView findCourseStudents(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("studentList", courseService.findStudentsByCourseId(courseId));
        mv.setViewName("instructor/course/students");
        return mv;
    }
}
