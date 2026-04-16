package com.wanted.projectmodule2lms.domain.comment.controller;

import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentDTO;
import com.wanted.projectmodule2lms.domain.comment.model.service.CommentService;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/regist")
    public String registerComment(@ModelAttribute CommentDTO commentDTO,
                                  @RequestParam Integer currentMemberId,
                                  @RequestParam MemberRole currentRole,
                                  RedirectAttributes redirectAttributes) {
        try {
            commentService.registComment(commentDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        }
    }

    @PostMapping("/modify")
    public String modifyComment(@ModelAttribute CommentDTO commentDTO,
                                @RequestParam Integer currentMemberId,
                                @RequestParam MemberRole currentRole,
                                RedirectAttributes redirectAttributes) {
        try {
            commentService.modifyComment(commentDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        }
    }

    @PostMapping("/delete")
    public String deleteComment(@RequestParam Integer commentId,
                                @RequestParam Integer postId,
                                @RequestParam Integer currentMemberId,
                                @RequestParam MemberRole currentRole,
                                RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(commentId, postId, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + postId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + postId;
        }
    }
}