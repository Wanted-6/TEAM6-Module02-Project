package com.wanted.projectmodule2lms.domain.board.controller;

import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import com.wanted.projectmodule2lms.domain.board.model.service.BoardService;
import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentDTO;
import com.wanted.projectmodule2lms.domain.comment.model.service.CommentService;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final CommentService commentService;
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
    public String courseNoticeListPage(@RequestParam(required = false) String keyword, Model model) {
        List<BoardDTO> boardList = (keyword == null || keyword.isBlank())
                ? boardService.findBoardByPostType(BoardType.COURSE_NOTICE)
                : boardService.searchBoardByPostTypeAndTitle(BoardType.COURSE_NOTICE, keyword);
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        return "board/course-notice-list";
    }

    @GetMapping("/admin-notices")
    public String adminNoticeListPage(@RequestParam(required = false) String keyword, Model model) {
        List<BoardDTO> boardList = (keyword == null || keyword.isBlank())
                ? boardService.findBoardByPostType(BoardType.ADMIN_NOTICE)
                : boardService.searchBoardByPostTypeAndTitle(BoardType.ADMIN_NOTICE, keyword);
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        return "board/admin-notice-list";
    }

    @GetMapping("/free")
    public String freeListPage(@RequestParam(required = false) String keyword, Model model) {
        List<BoardDTO> boardList = (keyword == null || keyword.isBlank())
                ? boardService.findBoardByPostType(BoardType.FREE)
                : boardService.searchBoardByPostTypeAndTitle(BoardType.FREE, keyword);
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        return "board/free-list";
    }

    @GetMapping("/section-qna")
    public String sectionQnaListPage(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer currentMemberId,
                                     @RequestParam(required = false) MemberRole currentRole,
                                     Model model) {
        List<BoardDTO> boardList = (currentMemberId != null && currentRole != null)
                ? boardService.findVisibleSectionQna(currentMemberId, currentRole, keyword)
                : ((keyword == null || keyword.isBlank())
                ? boardService.findBoardByPostType(BoardType.SECTION_QNA)
                : boardService.searchBoardByPostTypeAndTitle(BoardType.SECTION_QNA, keyword));
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        return "board/section-qna-list";
    }

    @GetMapping("/detail")
    public String boardDetailPage(@RequestParam Integer postId, Model model) {
        boardService.increaseViewCount(postId);
        BoardDTO board = boardService.findBoardById(postId);
        List<CommentDTO> commentList=commentService.findCommentsByPostId(postId);
        model.addAttribute("board", board);
        model.addAttribute("commentList", commentList);
        model.addAttribute("listPath", getListPath(board.getPostType()));
        return "board/detail";
    }

    @GetMapping("/regist")
    public String boardRegistPage(@RequestParam(defaultValue = "FREE") BoardType type,
                                  @RequestParam(required = false) Integer courseId,
                                  @RequestParam(required = false) Integer sectionId,
                                  @RequestParam(required = false) Integer currentMemberId,
                                  @RequestParam(required = false) MemberRole currentRole,
                                  Model model) {
        List<Course> courses = (currentMemberId != null && currentRole != null)
                ? boardService.findAvailableCourses(type, currentMemberId, currentRole)
                : boardService.findAllCourses();
        model.addAttribute("boardType", type);
        model.addAttribute("courses", courses);
        model.addAttribute("sections", (currentMemberId != null && currentRole != null)
                ? boardService.findAvailableSections(type, currentMemberId, currentRole)
                : List.of());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedSectionId", sectionId);
        model.addAttribute("listPath", getListPath(type));
        return "board/regist";
    }

    @PostMapping("/regist")
    public String registBoard(@ModelAttribute BoardDTO boardDTO,
                              @RequestParam Integer currentMemberId,
                              @RequestParam MemberRole currentRole,
                              RedirectAttributes redirectAttributes) {
        try {
            boardService.registBoard(boardDTO, currentMemberId, currentRole);
            return "redirect:" + getListPath(boardDTO.getPostType());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/regist?type=" + boardDTO.getPostType()
                    + appendNumberQuery("currentMemberId", currentMemberId)
                    + appendTextQuery("currentRole", currentRole != null ? currentRole.name() : null)
                    + appendNumberQuery("courseId", boardDTO.getCourseId())
                    + appendNumberQuery("sectionId", boardDTO.getSectionId());
        }
    }

    @GetMapping("/modify")
    public String boardModifyPage(@RequestParam Integer postId, Model model) {
        BoardDTO board = boardService.findBoardById(postId);
        model.addAttribute("board", board);
        model.addAttribute("listPath", getListPath(board.getPostType()));
        return "board/modify";
    }

    @PostMapping("/modify")
    public String modifyBoard(@ModelAttribute BoardDTO boardDTO,
                              @RequestParam Integer currentMemberId,
                              @RequestParam MemberRole currentRole,
                              RedirectAttributes redirectAttributes) {
        try {
            boardService.modifyBoard(boardDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + boardDTO.getPostId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/modify?postId=" + boardDTO.getPostId();
        }
    }

    @GetMapping("/delete")
    public String boardDeletePage(@RequestParam Integer postId, Model model) {
        BoardDTO board = boardService.findBoardById(postId);
        model.addAttribute("board", board);
        model.addAttribute("listPath", getListPath(board.getPostType()));
        return "board/delete";
    }

    @PostMapping("/delete")
    public String deleteBoard(@RequestParam Integer postId,
                              @RequestParam Integer currentMemberId,
                              @RequestParam MemberRole currentRole,
                              RedirectAttributes redirectAttributes) {
        BoardDTO board = boardService.findBoardById(postId);
        try {
            boardService.deleteBoard(postId, currentMemberId, currentRole);
            return "redirect:" + getListPath(board.getPostType());
        } catch (IllegalArgumentException e) {
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

    private String appendNumberQuery(String key, Integer value) {
        return value == null ? "" : "&" + key + "=" + value;
    }

    private String appendTextQuery(String key, String value) {
        return value == null || value.isBlank() ? "" : "&" + key + "=" + value;
    }
}
