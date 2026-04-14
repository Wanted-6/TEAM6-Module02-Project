package com.wanted.projectmodule2lms.domain.board.model.service;

import com.wanted.projectmodule2lms.domain.board.model.dao.BoardRepository;
import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.AnswerStatus;
import com.wanted.projectmodule2lms.domain.board.model.entity.Board;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    public List<BoardDTO> findBoardByPostType(BoardType postType) {
        return boardRepository.findByPostTypeAndIsDeletedFalse(postType).stream()
                .map(this::toBoardDTO)
                .collect(Collectors.toList());
    }

    public BoardDTO findBoardById(Integer postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        return toBoardDTO(board);
    }

    public List<BoardDTO> searchBoardByTitle(String title) {
        return boardRepository.findByTitleContainingAndIsDeletedFalse(title).stream()
                .map(this::toBoardDTO)
                .collect(Collectors.toList());
    }

    public List<BoardDTO> searchBoardByPostTypeAndTitle(BoardType postType, String title) {
        return boardRepository.findByPostTypeAndTitleContainingAndIsDeletedFalse(postType, title).stream()
                .map(this::toBoardDTO)
                .collect(Collectors.toList());
    }

    public List<Course> findAllCourses() {
        return courseRepository.findByIsOpenTrue();
    }

    @Transactional
    public Integer registBoard(BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        if (!canCreateBoard(currentRole, boardDTO.getPostType())) {
            throw new IllegalArgumentException("해당 게시판에 글을 등록할 권한이 없습니다.");
        }

        normalizeBoardDTO(boardDTO, currentMemberId);

        Board board = Board.builder()
                .memberId(boardDTO.getMemberId())
                .courseId(boardDTO.getCourseId())
                .sectionId(boardDTO.getSectionId())
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .postType(boardDTO.getPostType())
                .isSecret(boardDTO.getIsSecret())
                .answerStatus(boardDTO.getAnswerStatus())
                .viewCount(boardDTO.getViewCount())
                .isDeleted(boardDTO.getIsDeleted())
                .build();

        boardRepository.save(board);
        return board.getPostId();
    }

    @Transactional
    public void modifyBoard(BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        Board board = boardRepository.findById(boardDTO.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!canModifyOrDelete(currentMemberId, currentRole, board)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        AnswerStatus answerStatus = board.getPostType() == BoardType.SECTION_QNA
                ? boardDTO.getAnswerStatus()
                : null;

        Boolean isSecret = board.getPostType() == BoardType.SECTION_QNA && Boolean.TRUE.equals(boardDTO.getIsSecret());
        Integer courseId = needsCourseId(board.getPostType()) ? boardDTO.getCourseId() : null;
        Integer sectionId = board.getPostType() == BoardType.SECTION_QNA ? boardDTO.getSectionId() : null;

        board.modifyBoard(
                courseId,
                sectionId,
                boardDTO.getTitle(),
                boardDTO.getContent(),
                board.getPostType(),
                isSecret,
                answerStatus
        );
    }

    @Transactional
    public void deleteBoard(Integer postId, Integer currentMemberId, MemberRole currentRole) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!canModifyOrDelete(currentMemberId, currentRole, board)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        board.deleteBoard();
    }

    private BoardDTO toBoardDTO(Board board) {
        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);

        if (board.getCourseId() != null) {
            courseRepository.findById(board.getCourseId())
                    .map(Course::getTitle)
                    .ifPresent(boardDTO::setCourseTitle);
        }

        return boardDTO;
    }

    private boolean canCreateBoard(MemberRole currentRole, BoardType postType) {
        return switch (postType) {
            case ADMIN_NOTICE -> currentRole == MemberRole.ADMIN;
            case COURSE_NOTICE -> currentRole == MemberRole.INSTRUCTOR;
            case FREE -> true;
            case SECTION_QNA -> currentRole == MemberRole.STUDENT;
        };
    }

    private void normalizeBoardDTO(BoardDTO boardDTO, Integer currentMemberId) {
        boardDTO.setMemberId(currentMemberId);
        boardDTO.setViewCount(0);
        boardDTO.setIsDeleted(false);

        if (boardDTO.getPostType() == BoardType.ADMIN_NOTICE || boardDTO.getPostType() == BoardType.FREE) {
            boardDTO.setCourseId(null);
            boardDTO.setSectionId(null);
            boardDTO.setIsSecret(false);
            boardDTO.setAnswerStatus(null);
            return;
        }

        if (boardDTO.getPostType() == BoardType.COURSE_NOTICE) {
            if (boardDTO.getCourseId() == null) {
                throw new IllegalArgumentException("코스 공지는 코스를 선택해야 합니다.");
            }
            boardDTO.setSectionId(null);
            boardDTO.setIsSecret(false);
            boardDTO.setAnswerStatus(null);
            return;
        }

        if (boardDTO.getCourseId() == null || boardDTO.getSectionId() == null) {
            throw new IllegalArgumentException("섹션 Q&A는 코스와 섹션 정보를 모두 입력해야 합니다.");
        }
        boardDTO.setIsSecret(Boolean.TRUE.equals(boardDTO.getIsSecret()));
        boardDTO.setAnswerStatus(AnswerStatus.PENDING);
    }

    private boolean needsCourseId(BoardType postType) {
        return postType == BoardType.COURSE_NOTICE || postType == BoardType.SECTION_QNA;
    }

    private boolean canModifyOrDelete(Integer currentMemberId, MemberRole currentRole, Board board) {

        if (board.getPostType() == BoardType.ADMIN_NOTICE) {
            return currentRole == MemberRole.ADMIN
                    && currentMemberId.equals(board.getMemberId());
        }

        if (board.getPostType() == BoardType.COURSE_NOTICE) {
            return currentRole == MemberRole.INSTRUCTOR
                    && currentMemberId.equals(board.getMemberId());
        }

        if (board.getPostType() == BoardType.FREE) {
            return currentMemberId.equals(board.getMemberId());
        }

        if (board.getPostType() == BoardType.SECTION_QNA) {
            if (currentRole == MemberRole.INSTRUCTOR) {
                return true;
            }

            if (currentRole == MemberRole.STUDENT) {
                return currentMemberId.equals(board.getMemberId());
            }
        }

        return false;
    }
}
