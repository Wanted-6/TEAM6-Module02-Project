package com.wanted.projectmodule2lms.domain.board.controller;

import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import com.wanted.projectmodule2lms.domain.board.model.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @GetMapping({"", "/"})
    public String boardHomePage() {
        return "board/list";
    }

    @GetMapping("/list")
    public String boardListPage() {
        return "board/list";
    }


    @GetMapping("/course-notices")
    public String courseNoticeListPage() {
        return "board/course-notice-list";
    }

    @GetMapping("/free")
    public String freeListPage() {
        return "board/free-list";
    }

    @GetMapping("/section-qna")
    public String sectionQnaListPage() {
        return "board/section-qna-list";
    }

    @GetMapping("/detail")
    public String boardDetailPage(@RequestParam Integer postId, Model model) {
        BoardDTO board = boardService.findBoardById(postId);
        model.addAttribute("board", board);
        return "board/detail";
    }


    @GetMapping("/regist")
    public String boardRegistPage() {
        return "board/regist";
    }

    @GetMapping("/modify")
    public String boardModifyPage(@RequestParam(required = false) Integer postId) {
        return "board/modify";
    }

    @GetMapping("/delete")
    public String boardDeletePage(@RequestParam(required = false) Integer postId) {
        return "board/delete";
    }

    // 이전 경로 호환
    @GetMapping({"/posts", "/post-list.html"})
    public String postListCompatPage() {
        return "redirect:/board/list";
    }

    @GetMapping({"/posts/detail", "/post-detail.html"})
    public String postDetailCompatPage(@RequestParam(required = false) Integer postId) {
        return "redirect:/board/detail";
    }

    @GetMapping({"/posts/write", "/posts/form", "/post-form.html"})
    public String postWriteCompatPage() {
        return "redirect:/board/regist";
    }

    @GetMapping("/admin-notices")
    public String adminNoticeListPage(Model model) {
        List<BoardDTO> boardList = boardService.findBoardByPostType(BoardType.ADMIN_NOTICE);
        model.addAttribute("boardList", boardList);
        return "board/admin-notice-list";
    }
}
