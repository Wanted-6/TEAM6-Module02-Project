package com.wanted.projectmodule2lms.domain.board.controller;
import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
import com.wanted.projectmodule2lms.domain.board.model.dto.BoardViewDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import com.wanted.projectmodule2lms.domain.board.model.service.BoardService;
import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentThreadDTO;
import com.wanted.projectmodule2lms.domain.comment.model.service.CommentService;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.service.CurrentMemberService;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final CommentService commentService;
    private final BoardService boardService;
    private final CurrentMemberService currentMemberService;

    @ModelAttribute
    public void addCurrentMemberInfo(@LoginMemberId Long loginMemberId, Model model) {
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);
        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);

        model.addAttribute("currentMemberId", currentMemberId);
        model.addAttribute("currentRole", currentRole);
    }

    @GetMapping({"", "/"})
    public String boardHomePage() {
        return "board/list";
    }

    @GetMapping("/list")
    public String boardListPage() {
        return "board/list";
    }

    @AuditLog
    @GetMapping("/course-notices")
    public String courseNoticeListPage(@RequestParam(required = false) String keyword, Model model) {
        List<BoardViewDTO> boardList = (keyword == null || keyword.isBlank())
                ? boardService.findBoardByPostType(BoardType.COURSE_NOTICE)
                : boardService.searchBoardByPostTypeAndTitle(BoardType.COURSE_NOTICE, keyword);
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        return "board/course-notice-list";
    }

    @AuditLog
    @GetMapping("/admin-notices")
    public String adminNoticeListPage(@RequestParam(required = false) String keyword, Model model) {
        List<BoardViewDTO> boardList = (keyword == null || keyword.isBlank())
                ? boardService.findBoardByPostType(BoardType.ADMIN_NOTICE)
                : boardService.searchBoardByPostTypeAndTitle(BoardType.ADMIN_NOTICE, keyword);
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        return "board/admin-notice-list";
    }

    @AuditLog
    @GetMapping("/free")
    public String freeListPage(@RequestParam(required = false) String keyword, Model model) {
        List<BoardViewDTO> boardList = (keyword == null || keyword.isBlank())
                ? boardService.findBoardByPostType(BoardType.FREE)
                : boardService.searchBoardByPostTypeAndTitle(BoardType.FREE, keyword);
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        return "board/free-list";
    }

    @AuditLog
    @GetMapping("/section-qna")
    public String sectionQnaListPage(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer courseId,
                                     @LoginMemberId Long loginMemberId,
                                     Model model) {
        if (loginMemberId == null) {
            return "redirect:/auth/login";
        }
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);

        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);
        List<BoardViewDTO> boardList;

        if (currentRole != null) {
            boardList = boardService.findVisibleSectionQna(currentMemberId, currentRole, keyword);
        } else if (keyword == null || keyword.isBlank()) {
            boardList = boardService.findBoardByPostType(BoardType.SECTION_QNA);
        } else {
            boardList = boardService.searchBoardByPostTypeAndTitle(BoardType.SECTION_QNA, keyword);
        }

        if (courseId != null) {
            boardList.removeIf(board -> board.getCourseId() == null || !board.getCourseId().equals(courseId));
        }

        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("courseId", courseId);
        return "board/section-qna-list";
    }

    @AuditLog
    @GetMapping("/detail")
    public String boardDetailPage(@RequestParam Integer postId, Model model) {
        BoardViewDTO board = boardService.findBoardById(postId);
        List<CommentThreadDTO> commentThreads = commentService.findCommentThreadsByPostId(postId);
        model.addAttribute("board", board);
        model.addAttribute("commentThreads", commentThreads);
        model.addAttribute("listPath", getListPath(board.getPostType(), board.getCourseId()));
        return "board/detail";
    }

    @PostMapping("/view-count")
    @ResponseBody
    public void viewCount(@RequestParam Integer postId) {
        boardService.increaseViewCount(postId);
    }

    @GetMapping("/regist")
    public String boardRegistPage(@RequestParam(defaultValue = "FREE") BoardType type,
                                  @RequestParam(required = false) Integer courseId,
                                  @RequestParam(required = false) Integer sectionId,
                                  @LoginMemberId Long loginMemberId,
                                  Model model) {
        if (loginMemberId == null) {
            return "redirect:/auth/login";
        }
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);

        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);

        List<Course> courses = boardService.findAvailableCourses(type, currentMemberId, currentRole);
        String selectedCourseTitle = courses.stream()
                .filter(course -> courseId != null && course.getCourseId().equals(courseId))
                .map(Course::getTitle)
                .findFirst()
                .orElse(null);
        model.addAttribute("boardType", type);
        model.addAttribute("courses", courses);
        model.addAttribute("sections",
                boardService.findAvailableSections(type, currentMemberId, currentRole));
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedCourseTitle", selectedCourseTitle);
        model.addAttribute("selectedSectionId", sectionId);
        model.addAttribute("listPath", getListPath(type, courseId));
        return "board/regist";
    }

    @PostMapping("/regist")
    public String registBoard(@LoginMemberId Long loginMemberId,
                              @ModelAttribute BoardDTO boardDTO,
                              RedirectAttributes redirectAttributes) {
        if (loginMemberId == null) {
            return "redirect:/auth/login";
        }
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);

        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);

        try {
            boardService.registBoard(boardDTO, currentMemberId, currentRole);
            return "redirect:" + getListPath(boardDTO.getPostType(), boardDTO.getCourseId());
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/regist?type=" + boardDTO.getPostType()
                    + appendNumberQuery("courseId", boardDTO.getCourseId())
                    + appendNumberQuery("sectionId", boardDTO.getSectionId());
        }
    }

    @GetMapping("/modify")
    public String boardModifyPage(@RequestParam Integer postId, Model model) {
        BoardViewDTO board = boardService.findBoardById(postId);
        model.addAttribute("board", board);
        model.addAttribute("listPath", getListPath(board.getPostType(), board.getCourseId()));
        return "board/modify";
    }


    @PostMapping("/modify")
    public String modifyBoard(@LoginMemberId Long loginMemberId,
                              @ModelAttribute BoardDTO boardDTO,
                              RedirectAttributes redirectAttributes) {
        if (loginMemberId == null) {
            return "redirect:/auth/login";
        }
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);
        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);

        try {
            boardService.modifyBoard(boardDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + boardDTO.getPostId();
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/modify?postId=" + boardDTO.getPostId();
        }
    }

    @GetMapping("/delete")
    public String boardDeletePage(@RequestParam Integer postId, Model model) {
        BoardViewDTO board = boardService.findBoardById(postId);
        model.addAttribute("board", board);
        model.addAttribute("listPath", getListPath(board.getPostType(), board.getCourseId()));
        return "board/delete";
    }


    @PostMapping("/delete")
    public String deleteBoard(@LoginMemberId Long loginMemberId,
                              @RequestParam Integer postId,
                              RedirectAttributes redirectAttributes) {
        if (loginMemberId == null) {
            return "redirect:/auth/login";
        }
        Integer currentMemberId = currentMemberService.toMemberId(loginMemberId);

        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);
        BoardViewDTO board = boardService.findBoardById(postId);
        try {
            boardService.deleteBoard(postId, currentMemberId, currentRole);
            return "redirect:" + getListPath(board.getPostType(), board.getCourseId());
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + postId;
        }
    }

    @GetMapping({"/posts", "/post-list.html"})
    public String postListCompatPage() {
        return "redirect:/board/list";
    }

    @GetMapping({"/posts/detail", "/post-detail.html"})
    public String postDetailCompatPage(@RequestParam(required = false) Integer postId) {
        if (postId == null) {
            return "redirect:/board/list";
        }
        return "redirect:/board/detail?postId=" + postId;
    }

    @GetMapping({"/posts/write", "/posts/form", "/post-form.html"})
    public String postWriteCompatPage() {
        return "redirect:/board/regist?type=FREE";
    }

    private String getListPath(BoardType boardType) {
        return switch (boardType) {
            case ADMIN_NOTICE -> "/board/admin-notices";
            case COURSE_NOTICE -> "/board/course-notices";
            case FREE -> "/board/free";
            case SECTION_QNA -> "/board/section-qna";
        };
    }

    private String getListPath(BoardType boardType, Integer courseId) {
        String basePath = getListPath(boardType);

        if (boardType != BoardType.SECTION_QNA) {
            return basePath;
        }

        StringBuilder pathBuilder = new StringBuilder(basePath);
        appendQuery(pathBuilder, "courseId", courseId, false);

        return pathBuilder.toString();
    }

    private String appendNumberQuery(String key, Integer value) {
        return value == null ? "" : "&" + key + "=" + value;
    }

    private boolean appendQuery(StringBuilder pathBuilder, String key, Integer value, boolean hasQuery) {
        if (value == null) {
            return hasQuery;
        }

        pathBuilder.append(hasQuery ? "&" : "?")
                .append(key)
                .append("=")
                .append(value);
        return true;
    }

}
