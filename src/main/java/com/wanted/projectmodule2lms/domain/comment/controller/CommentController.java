package com.wanted.projectmodule2lms.domain.comment.controller;

import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentDTO;
import com.wanted.projectmodule2lms.domain.comment.model.service.CommentService;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;
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
    private final MemberRepository memberRepository;


    @PostMapping("/regist")
    public String registerComment(@LoginMemberId Long loginMemberId,
                                  @ModelAttribute CommentDTO commentDTO,
                                  RedirectAttributes redirectAttributes) {
        Integer currentMemberId = loginMemberId != null ? loginMemberId.intValue() : null;
        MemberRole currentRole = getCurrentMemberRole();

        try {
            commentService.registComment(commentDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        }
    }


    @PostMapping("/modify")
    public String modifyComment(@LoginMemberId Long loginMemberId,
                                @ModelAttribute CommentDTO commentDTO,
                                RedirectAttributes redirectAttributes) {
        Integer currentMemberId = loginMemberId != null ? loginMemberId.intValue() : null;
        MemberRole currentRole = getCurrentMemberRole();

        try {
            commentService.modifyComment(commentDTO, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + commentDTO.getPostId();
        }
    }

 
    @PostMapping("/delete")
    public String deleteComment(@LoginMemberId Long loginMemberId,
                                @RequestParam Integer commentId,
                                @RequestParam Integer postId,
                                RedirectAttributes redirectAttributes) {
        Integer currentMemberId = loginMemberId != null ? loginMemberId.intValue() : null;
        MemberRole currentRole = getCurrentMemberRole();

        try {
            commentService.deleteComment(commentId, postId, currentMemberId, currentRole);
            return "redirect:/board/detail?postId=" + postId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/detail?postId=" + postId;
        }
    }

    private MemberRole getCurrentMemberRole() {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        Integer currentMemberId = loginMemberId != null ? loginMemberId.intValue() : null;

        if (currentMemberId == null) {
            return null;
        }

        Member member = memberRepository.findById(currentMemberId).orElse(null);
        return member != null ? member.getRole() : null;
    }
}
