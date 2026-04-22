package com.wanted.projectmodule2lms.domain.comment.controller;

import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentDTO;
import com.wanted.projectmodule2lms.domain.comment.model.service.CommentService;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.service.CurrentMemberService;
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
    private final CurrentMemberService currentMemberService;

    @PostMapping("/regist")
    public String registerComment(@LoginMemberId Long loginMemberId,
                                  @ModelAttribute CommentDTO commentDTO,
                                  RedirectAttributes redirectAttributes) {
        Integer currentMemberId = requireMemberId(loginMemberId);
        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);

        try {
            commentService.registComment(commentDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        }
    }

    @PostMapping("/modify")
    public String modifyComment(@LoginMemberId Long loginMemberId,
                                @ModelAttribute CommentDTO commentDTO,
                                RedirectAttributes redirectAttributes) {
        Integer currentMemberId = requireMemberId(loginMemberId);
        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);

        try {
            commentService.modifyComment(commentDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        }
    }

    @PostMapping("/delete")
    public String deleteComment(@LoginMemberId Long loginMemberId,
                                @RequestParam Integer commentId,
                                @RequestParam Integer postId,
                                RedirectAttributes redirectAttributes) {
        Integer currentMemberId = requireMemberId(loginMemberId);
        MemberRole currentRole = currentMemberService.getCurrentMemberRole(currentMemberId);

        try {
            commentService.deleteComment(commentId, postId, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + postId;
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + postId;
        }
    }

    private Integer requireMemberId(Long loginMemberId) {
        if (loginMemberId == null) {
            throw new LoginRequiredException("로그인한 사용자 정보가 필요합니다.");
        }
        return currentMemberService.toMemberId(loginMemberId);
    }
}