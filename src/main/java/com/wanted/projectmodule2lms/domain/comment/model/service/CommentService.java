package com.wanted.projectmodule2lms.domain.comment.model.service;

import com.wanted.projectmodule2lms.domain.board.model.dto.BoardViewDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import com.wanted.projectmodule2lms.domain.board.model.service.BoardService;
import com.wanted.projectmodule2lms.domain.comment.model.dao.CommentRepository;
import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentDTO;
import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentThreadDTO;
import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentViewDTO;
import com.wanted.projectmodule2lms.domain.comment.model.entity.Comment;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final BoardService boardService;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<CommentViewDTO> findCommentsByPostId(Integer postId) {
        List<Comment> commentList = commentRepository.findByPostIdAndIsDeletedFalseOrderByCommentIdAsc(postId);

        Set<Integer> memberIds = commentList.stream()
                .map(Comment::getMemberId)
                .collect(Collectors.toSet());

        Map<Integer, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, member -> member));

        return commentList.stream()
                .map(comment -> {
                    Member member = memberMap.get(comment.getMemberId());
                    return toCommentViewDTO(comment, member);
                })
                .collect(Collectors.toList());
    }

    private CommentViewDTO toCommentViewDTO(Comment comment, Member member) {
        String memberName = member != null ? member.getName() : null;
        String profileImage = member != null && member.getProfile() != null
                ? member.getProfile().getProfileImage()
                : null;

        return new CommentViewDTO(
                comment.getCommentId(),
                comment.getPostId(),
                comment.getMemberId(),
                comment.getParentCommentId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                memberName,
                profileImage
        );
    }


    @Transactional
    public void registComment(CommentDTO commentDTO, Integer currentMemberId, MemberRole currentRole) {
        BoardViewDTO board = boardService.findBoardById(commentDTO.getPostId());

        if (!canCreateComment(currentMemberId, currentRole, board)) {
            throw new IllegalArgumentException("댓글 작성 권한이 없습니다.");
        }

        Comment comment = new Comment(
                commentDTO.getPostId(),
                currentMemberId,
                commentDTO.getParentCommentId(),
                commentDTO.getContent()
        );
        commentRepository.save(comment);
        if(board.getPostType() == BoardType.SECTION_QNA && currentRole == MemberRole.INSTRUCTOR) {
            boardService.changeAnswerStatusToAnswered(commentDTO.getPostId());
        }
    }

    @Transactional
    public void modifyComment(CommentDTO commentDTO, Integer currentMemberId, MemberRole currentRole) {
        Comment comment = commentRepository.findById(commentDTO.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        BoardViewDTO board = boardService.findBoardById(comment.getPostId());

        if (!canModifyOrDeleteComment(currentMemberId, currentRole, comment, board)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.changeContent(commentDTO.getContent());
    }

    @Transactional
    public void deleteComment(Integer commentId, Integer postId, Integer currentMemberId, MemberRole currentRole) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        BoardViewDTO board = boardService.findBoardById(comment.getPostId());

        if (!canModifyOrDeleteComment(currentMemberId, currentRole, comment, board)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        comment.deleteComment();
    }

    private boolean canCreateComment(Integer currentMemberId, MemberRole currentRole, BoardViewDTO board) {
        if (board.getPostType() != BoardType.SECTION_QNA) {
            return true;
        }

        if (currentRole == MemberRole.ADMIN) {
            return true;
        }

        if (board.getCourseId() == null) {
            return false;
        }

        if (currentRole == MemberRole.INSTRUCTOR) {
            return isInstructorCourse(board.getCourseId(), currentMemberId);
        }

        if (currentRole == MemberRole.STUDENT) {
            return isEnrolledCourse(board.getCourseId(), currentMemberId);
        }

        return false;
    }

    private boolean canModifyOrDeleteComment(Integer currentMemberId,
                                             MemberRole currentRole,
                                             Comment comment,
                                             BoardViewDTO board) {
        if (currentRole == MemberRole.ADMIN) {
            return true;
        }

        if (currentMemberId.equals(comment.getMemberId())) {
            if (board.getPostType() != BoardType.SECTION_QNA) {
                return true;
            }

            return board.getCourseId() != null && isEnrolledCourse(board.getCourseId(), currentMemberId);
        }

        if (board.getPostType() == BoardType.COURSE_NOTICE && currentRole == MemberRole.INSTRUCTOR) {
            return board.getCourseId() != null && isInstructorCourse(board.getCourseId(), currentMemberId);
        }

        if (board.getPostType() == BoardType.SECTION_QNA && currentRole == MemberRole.INSTRUCTOR) {
            return board.getCourseId() != null && isInstructorCourse(board.getCourseId(), currentMemberId);
        }

        return false;
    }

    private boolean isInstructorCourse(Integer courseId, Integer currentMemberId) {
        return courseRepository.findById(courseId)
                .map(course -> course.getInstructorId().equals(currentMemberId))
                .orElse(false);
    }

    private boolean isEnrolledCourse(Integer courseId, Integer currentMemberId) {
        return enrollmentRepository.findByMemberId(currentMemberId).stream()
                .map(Enrollment::getCourseId)
                .anyMatch(courseId::equals);
    }

    public List<CommentThreadDTO> findCommentThreadsByPostId(Integer postId) {
        List<CommentViewDTO> comments = findCommentsByPostId(postId);

        List<CommentViewDTO> parents = comments.stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .toList();

        Map<Integer, List<CommentViewDTO>> replyMap = comments.stream()
                .filter(comment -> comment.getParentCommentId() != null)
                .collect(Collectors.groupingBy(CommentViewDTO::getParentCommentId));

        return parents.stream()
                .map(parent -> new CommentThreadDTO(
                        parent,
                        replyMap.getOrDefault(parent.getCommentId(), Collections.emptyList())
                ))
                .toList();
    }


}
