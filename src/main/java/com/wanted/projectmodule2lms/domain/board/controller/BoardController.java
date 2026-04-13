package com.wanted.projectmodule2lms.domain.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

    @GetMapping({"/board", "/board/", "/board.html"})
    public String boardPage() {
        return "board/board";
    }

    @GetMapping({"/board/admin-notices", "/board/admin-notices/list"})
    public String adminNoticeListPage() {
        return "board/admin-notice-list";
    }

    @GetMapping("/board/admin-notices/detail")
    public String adminNoticeDetailPage() {
        return "board/admin-notice-detail";
    }

    @GetMapping("/board/admin-notices/form")
    public String adminNoticeFormPage() {
        return "board/admin-notice-form";
    }

    @GetMapping({"/board/course-notices", "/board/course-notices/list"})
    public String courseNoticeListPage() {
        return "board/course-notice-list";
    }

    @GetMapping("/board/course-notices/detail")
    public String courseNoticeDetailPage() {
        return "board/course-notice-detail";
    }

    @GetMapping("/board/course-notices/form")
    public String courseNoticeFormPage() {
        return "board/course-notice-form";
    }

    @GetMapping({"/board/course-questions", "/board/course-questions/list"})
    public String courseQuestionListPage() {
        return "board/course-question-list";
    }

    @GetMapping("/board/course-questions/detail")
    public String courseQuestionDetailPage() {
        return "board/course-question-detail";
    }

    @GetMapping("/board/course-questions/form")
    public String courseQuestionFormPage() {
        return "board/course-question-form";
    }

    @GetMapping({"/board/free", "/board/free/list"})
    public String freeListPage() {
        return "board/free-list";
    }

    @GetMapping("/board/free/detail")
    public String freeDetailPage() {
        return "board/free-detail";
    }

    @GetMapping("/board/free/form")
    public String freeFormPage() {
        return "board/free-form";
    }

    @GetMapping({"/board/section-qna", "/board/section-qna/list"})
    public String sectionQnaListPage() {
        return "board/section-qna-list";
    }

    @GetMapping("/board/section-qna/detail")
    public String sectionQnaDetailPage() {
        return "board/section-qna-detail";
    }

    @GetMapping("/board/section-qna/form")
    public String sectionQnaFormPage() {
        return "board/section-qna-form";
    }
}
