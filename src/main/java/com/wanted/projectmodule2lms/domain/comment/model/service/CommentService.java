package com.wanted.projectmodule2lms.domain.comment.model.service;

import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import com.wanted.projectmodule2lms.domain.board.model.service.BoardService;
import com.wanted.projectmodule2lms.domain.comment.model.dao.CommentRepository;
import com.wanted.projectmodule2lms.domain.comment.model.dto.CommentDTO;
import com.wanted.projectmodule2lms.domain.comment.model.entity.Comment;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final BoardService boardService;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

        public List<CommentDTO> findCommentsByPostId(Integer postId) {
        List<Comment> commentList = commentRepository.findByPostIdAndIsDeletedFalseOrderByCommentIdAsc(postId);

        return commentList.stream()
                .map(comment -> {
                    String memberName = memberRepository.findById(comment.getMemberId())
                            .map(Member::getName)
                            .orElse(null);

                    return new CommentDTO(
                            comment.getCommentId(),
                            comment.getPostId(),
                            comment.getMemberId(),
                            comment.getParentCommentId(),
                            comment.getContent(),
                            comment.getIsDeleted(),
                            comment.getCreatedAt(),
                            comment.getUpdatedAt(),
                            memberName
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void registComment(CommentDTO commentDTO, Integer currentMemberId, MemberRole currentRole) {
        BoardDTO board = boardService.findBoardById(commentDTO.getPostId());

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
        BoardDTO board = boardService.findBoardById(comment.getPostId());

        if (!canModifyOrDeleteComment(currentMemberId, currentRole, comment, board)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.changeContent(commentDTO.getContent());
    }

    @Transactional
    public void deleteComment(Integer commentId, Integer postId, Integer currentMemberId, MemberRole currentRole) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        BoardDTO board = boardService.findBoardById(comment.getPostId());

        if (!canModifyOrDeleteComment(currentMemberId, currentRole, comment, board)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        comment.deleteComment();
    }

    private boolean canCreateComment(Integer currentMemberId, MemberRole currentRole, BoardDTO board) {
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
                                             BoardDTO board) {
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
}
