package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.model.dto.CourseCreateDTO;
import com.wanted.projectmodule2lms.domain.course.model.dto.CourseUpdateDTO;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    /* 전체 코스 조회 */
    @GetMapping
    public ModelAndView findAllCourses(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       ModelAndView mv) {
        mv.addObject("courseList", courseService.findAllCourses(keyword, category));
        mv.addObject("keyword", keyword);
        mv.addObject("category", category);
        mv.setViewName("instructor/course/list");
        return mv;
    }

    /* 코스 상세 조회 */
    @GetMapping("/{courseId}")
    public ModelAndView findCourseById(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.setViewName("instructor/course/detail");
        return mv;
    }

    /* 코스 등록 페이지 */
    @GetMapping("/regist")
    public String registPage() {
        return "instructor/course/regist";
    }

    /* 코스 등록 */
    @PostMapping("/regist")
    public ModelAndView registCourse(@ModelAttribute CourseCreateDTO createDTO,
                                     ModelAndView mv) {
        Integer courseId = courseService.registCourse(createDTO);
        mv.setViewName("redirect:/courses/" + courseId);
        return mv;
    }

    /* 코스 수정 페이지 */
    @GetMapping("/{courseId}/modify")
    public ModelAndView modifyPage(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.setViewName("instructor/course/modify");
        return mv;
    }

    /* 코스 수정 */
    @PostMapping("/{courseId}/modify")
    public String modifyCourse(@PathVariable Integer courseId,
                               @ModelAttribute CourseUpdateDTO updateDTO,
                               RedirectAttributes rttr) {
        courseService.modifyCourse(courseId, updateDTO);
        rttr.addFlashAttribute("successMessage", "코스가 수정되었습니다.");
        return "redirect:/courses/" + courseId;
    }

    @GetMapping("/{courseId}/students")
    public ModelAndView findCourseStudents(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("studentList", courseService.findStudentsByCourseId(courseId));
        mv.setViewName("instructor/course/students");
        return mv;
    }

}