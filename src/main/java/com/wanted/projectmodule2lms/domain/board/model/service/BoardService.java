package com.wanted.projectmodule2lms.domain.board.model.service;

import com.wanted.projectmodule2lms.domain.board.model.dao.BoardRepository;
import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
import com.wanted.projectmodule2lms.domain.board.model.dto.BoardViewDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.AnswerStatus;
import com.wanted.projectmodule2lms.domain.board.model.entity.Board;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class    BoardService {

    private final BoardRepository boardRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final MemberRepository memberRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;

    public List<BoardViewDTO> findBoardByPostType(BoardType postType) {
        return toBoardViewDTOList(boardRepository.findByPostTypeAndIsDeletedFalse(postType));
    }

    public List<BoardViewDTO> searchBoardByTitle(String title) {
        return toBoardViewDTOList(boardRepository.findByTitleContainingAndIsDeletedFalse(title));
    }

    public List<BoardViewDTO> searchBoardByPostTypeAndTitle(BoardType postType, String title) {
        return toBoardViewDTOList(boardRepository.findByPostTypeAndTitleContainingAndIsDeletedFalse(postType, title));
    }

    public List<BoardViewDTO> findVisibleSectionQna(Integer currentMemberId, MemberRole currentRole, String keyword) {
        List<Board> boards = (keyword == null || keyword.isBlank())
                ? boardRepository.findByPostTypeAndIsDeletedFalse(BoardType.SECTION_QNA)
                : boardRepository.findByPostTypeAndTitleContainingAndIsDeletedFalse(BoardType.SECTION_QNA, keyword);

        Set<Integer> allowedCourseIds = getAllowedCourseIdsForSectionQna(currentMemberId, currentRole);

        return toBoardViewDTOList(boards.stream()
                .filter(board -> board.getCourseId() != null && allowedCourseIds.contains(board.getCourseId()))
                .collect(Collectors.toList()));
    }

    public BoardViewDTO findBoardById(Integer postId) {
        return toBoardViewDTO(findActiveBoard(postId));
    }

    public List<Course> findAllCourses() {
        return courseRepository.findByIsOpenTrue();
    }

    public List<Course> findAvailableCourses(BoardType boardType, Integer currentMemberId, MemberRole currentRole) {
        if (boardType == BoardType.COURSE_NOTICE && currentRole == MemberRole.INSTRUCTOR) {
            return courseRepository.findByInstructorId(currentMemberId).stream()
                    .filter(course -> Boolean.TRUE.equals(course.getIsOpen()))
                    .collect(Collectors.toList());
        }

        if (boardType == BoardType.SECTION_QNA && currentRole == MemberRole.STUDENT) {
            Set<Integer> courseIds = getEnrolledCourseIds(currentMemberId);
            if (courseIds.isEmpty()) {
                return Collections.emptyList();
            }

            return courseRepository.findAllById(courseIds).stream()
                    .filter(course -> Boolean.TRUE.equals(course.getIsOpen()))
                    .collect(Collectors.toList());
        }

        return findAllCourses();
    }

    public List<Section> findAvailableSections(BoardType boardType, Integer currentMemberId, MemberRole currentRole) {
        if (boardType != BoardType.SECTION_QNA) {
            return Collections.emptyList();
        }

        Set<Integer> courseIds = getAllowedCourseIdsForSectionQna(currentMemberId, currentRole);
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        return sectionRepository.findByCourseIdInOrderByCourseIdAscSectionOrderAsc(courseIds);
    }

    @Transactional
    public Integer registBoard(BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        if (!canCreateBoard(currentRole, boardDTO.getPostType())) {
            throw new IllegalArgumentException("등록 권한이 없습니다.");
        }

        BoardDTO normalizedBoardDTO = normalizeBoardDTO(boardDTO, currentMemberId, currentRole);
        Board board = modelMapper.map(normalizedBoardDTO, Board.class);


        boardRepository.save(board);
        return board.getPostId();
    }

    @Transactional
    public void modifyBoard(BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        Board board = findActiveBoard(boardDTO.getPostId());

        if (!canModifyOrDelete(currentMemberId, currentRole, board)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        Integer courseId = resolveModifiedCourseId(board, boardDTO, currentMemberId, currentRole);
        Integer sectionId = resolveModifiedSectionId(board, boardDTO, currentMemberId, currentRole);
        Boolean isSecret = resolveModifiedSecret(board, boardDTO);
        AnswerStatus answerStatus = resolveModifiedAnswerStatus(board, boardDTO);
        String title = boardDTO.getTitle();

        if (board.getPostType() == BoardType.SECTION_QNA) {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new IllegalArgumentException("유효한 섹션을 선택해야 합니다."));
            title = formatSectionQnaTitle(section.getTitle(), boardDTO.getTitle());
        }

        board.modifyBoard(
                courseId,
                sectionId,
                title,
                boardDTO.getContent(),
                board.getPostType(),
                isSecret,
                answerStatus
        );
    }

    @Transactional
    public void deleteBoard(Integer postId, Integer currentMemberId, MemberRole currentRole) {
        Board board = findActiveBoard(postId);

        if (!canModifyOrDelete(currentMemberId, currentRole, board)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        board.deleteBoard();
    }

    public Board findActiveBoard(Integer postId) {
        return boardRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    }

    private BoardDTO normalizeBoardDTO(BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        Integer memberId = currentMemberId;
        Integer viewCount = 0;
        Boolean isDeleted = false;

        Integer courseId = boardDTO.getCourseId();
        Integer sectionId = boardDTO.getSectionId();
        Boolean isSecret = boardDTO.getIsSecret();
        AnswerStatus answerStatus = boardDTO.getAnswerStatus();
        String title = boardDTO.getTitle();

        if (boardDTO.getPostType() == BoardType.ADMIN_NOTICE || boardDTO.getPostType() == BoardType.FREE) {
            courseId = null;
            sectionId = null;
            isSecret = false;
            answerStatus = null;
        } else if (boardDTO.getPostType() == BoardType.COURSE_NOTICE) {
            if (courseId == null) {
                throw new IllegalArgumentException("코스를 선택해야 합니다.");
            }
            if (!isInstructorCourse(courseId, currentMemberId)) {
                throw new IllegalArgumentException("해당 코스만 공지를 등록할 수 있습니다.");
            }

            sectionId = null;
            isSecret = false;
            answerStatus = null;
        } else {
            validateSectionQnaAccess(courseId, sectionId, currentMemberId, currentRole, true);
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new IllegalArgumentException("유효한 섹션을 선택해야 합니다."));

            title = formatSectionQnaTitle(section.getTitle(), title);
            isSecret = Boolean.TRUE.equals(isSecret);
            answerStatus = AnswerStatus.PENDING;
        }

        return new BoardDTO(
                boardDTO.getPostId(),
                memberId,
                boardDTO.getMemberName(),
                courseId,
                boardDTO.getCourseTitle(),
                boardDTO.getCourseInstructorId(),
                sectionId,
                boardDTO.getSectionTitle(),
                title,
                boardDTO.getContent(),
                boardDTO.getPostType(),
                isSecret,
                answerStatus,
                viewCount,
                isDeleted,
                boardDTO.getCreatedAt(),
                boardDTO.getUpdatedAt(),
                boardDTO.getProfileImage()
        );
    }

    private boolean needsCourseId(BoardType postType) {
        return postType == BoardType.COURSE_NOTICE || postType == BoardType.SECTION_QNA;
    }

    private Integer resolveModifiedCourseId(Board board, BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        if (!needsCourseId(board.getPostType())) {
            return null;
        }

        if (boardDTO.getCourseId() == null) {
            throw new IllegalArgumentException("코스 정보가 없는 게시글입니다.");
        }

        if (board.getPostType() == BoardType.COURSE_NOTICE && !isInstructorCourse(boardDTO.getCourseId(), currentMemberId)) {
            throw new IllegalArgumentException("담당 코스 공지만 수정할 수 있습니다.");
        }

        if (board.getPostType() == BoardType.SECTION_QNA) {
            validateSectionQnaAccess(boardDTO.getCourseId(), boardDTO.getSectionId(), currentMemberId, currentRole, false);
        }

        if (board.getPostType() == BoardType.SECTION_QNA && !boardDTO.getCourseId().equals(board.getCourseId())) {
            throw new IllegalArgumentException("섹션 Q&A의 코스는 변경할 수 없습니다.");
        }

        return boardDTO.getCourseId();
    }

    private Integer resolveModifiedSectionId(Board board, BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        if (board.getPostType() != BoardType.SECTION_QNA) {
            return null;
        }

        if (boardDTO.getSectionId() == null) {
            throw new IllegalArgumentException("섹션을 선택해야 합니다.");
        }

        validateSectionQnaAccess(board.getCourseId(), boardDTO.getSectionId(), currentMemberId, currentRole, false);

        if (!boardDTO.getSectionId().equals(board.getSectionId())) {
            throw new IllegalArgumentException("섹션 Q&A의 섹션은 변경할 수 없습니다.");
        }

        return boardDTO.getSectionId();
    }

    private Boolean resolveModifiedSecret(Board board, BoardDTO boardDTO) {
        return board.getPostType() == BoardType.SECTION_QNA && Boolean.TRUE.equals(boardDTO.getIsSecret());
    }

    private AnswerStatus resolveModifiedAnswerStatus(Board board, BoardDTO boardDTO) {
        if (board.getPostType() != BoardType.SECTION_QNA) {
            return null;
        }

        return boardDTO.getAnswerStatus() != null ? boardDTO.getAnswerStatus() : board.getAnswerStatus();
    }

    private boolean canCreateBoard(MemberRole currentRole, BoardType postType) {
        return switch (postType) {
            case ADMIN_NOTICE -> currentRole == MemberRole.ADMIN;
            case COURSE_NOTICE -> currentRole == MemberRole.INSTRUCTOR;
            case FREE -> true;
            case SECTION_QNA -> currentRole == MemberRole.STUDENT;
        };
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
            return currentRole == MemberRole.ADMIN
                    || currentMemberId.equals(board.getMemberId());
        }

        if (board.getPostType() == BoardType.SECTION_QNA) {
            if (currentRole == MemberRole.INSTRUCTOR) {
                return board.getCourseId() != null && isInstructorCourse(board.getCourseId(), currentMemberId);
            }

            if (currentRole == MemberRole.STUDENT) {
                return currentMemberId.equals(board.getMemberId())
                        && board.getCourseId() != null
                        && getEnrolledCourseIds(currentMemberId).contains(board.getCourseId());
            }
        }

        return false;
    }

    private Set<Integer> getAllowedCourseIdsForSectionQna(Integer currentMemberId, MemberRole currentRole) {
        if (currentRole == MemberRole.ADMIN) {
            return courseRepository.findByIsOpenTrue().stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (currentRole == MemberRole.INSTRUCTOR) {
            return courseRepository.findByInstructorId(currentMemberId).stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (currentRole == MemberRole.STUDENT) {
            return getEnrolledCourseIds(currentMemberId);
        }

        return Collections.emptySet();
    }

    private Set<Integer> getEnrolledCourseIds(Integer currentMemberId) {
        return enrollmentRepository.findByMemberId(currentMemberId).stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isInstructorCourse(Integer courseId, Integer currentMemberId) {
        return courseRepository.findById(courseId)
                .map(Course::getInstructorId)
                .map(instructorId -> instructorId.equals(currentMemberId))
                .orElse(false);
    }

    private void validateSectionQnaAccess(Integer courseId,
                                          Integer sectionId,
                                          Integer currentMemberId,
                                          MemberRole currentRole,
                                          boolean isCreate) {
        if (courseId == null || sectionId == null) {
            throw new IllegalArgumentException("코스와 섹션을 모두 선택해야 합니다.");
        }

        Set<Integer> allowedCourseIds = getAllowedCourseIdsForSectionQna(currentMemberId, currentRole);
        if (!allowedCourseIds.contains(courseId)) {
            throw new IllegalArgumentException(isCreate
                    ? "수강 중인 코스에만 섹션 질문을 등록할 수 있습니다."
                    : "수강 중인 코스의 섹션 질문만 수정할 수 있습니다.");
        }

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 섹션입니다."));

        if (!section.getCourseId().equals(courseId)) {
            throw new IllegalArgumentException("선택한 섹션이 해당 코스에 속하지 않습니다.");
        }
    }

    private BoardViewDTO toBoardViewDTO(Board board) {
        return toBoardViewDTO(
                board,
                loadMemberNameMap(Collections.singletonList(board)),
                loadMemberProfileImageMap(Collections.singletonList(board)),
                loadCourseTitleMap(Collections.singletonList(board)),
                loadCourseInstructorMap(Collections.singletonList(board)),
                loadSectionTitleMap(Collections.singletonList(board))
        );
    }

    private List<BoardViewDTO> toBoardViewDTOList(List<Board> boards) {
        Map<Integer, String> memberNameMap = loadMemberNameMap(boards);
        Map<Integer, String> memberProfileImageMap = loadMemberProfileImageMap(boards);
        Map<Integer, String> courseTitleMap = loadCourseTitleMap(boards);
        Map<Integer, Integer> courseInstructorMap = loadCourseInstructorMap(boards);
        Map<Integer, String> sectionTitleMap = loadSectionTitleMap(boards);

        return boards.stream()
                .map(board -> toBoardViewDTO(board, memberNameMap, memberProfileImageMap, courseTitleMap, courseInstructorMap, sectionTitleMap))
                .collect(Collectors.toList());
    }

    private BoardViewDTO toBoardViewDTO(Board board,
                                        Map<Integer, String> memberNameMap,
                                        Map<Integer, String> memberProfileImageMap,
                                        Map<Integer, String> courseTitleMap,
                                        Map<Integer, Integer> courseInstructorMap,
                                        Map<Integer, String> sectionTitleMap) {

        String memberName = resolveMemberName(board.getMemberId(), memberNameMap);
        String profileImage = resolveMemberProfileImage(board.getMemberId(), memberProfileImageMap);

        String courseTitle = null;
        Integer courseInstructorId = null;
        if (board.getCourseId() != null) {
            courseTitle = resolveCourseTitle(board.getCourseId(), courseTitleMap);
            courseInstructorId = resolveCourseInstructorId(board.getCourseId(), courseInstructorMap);
        }

        String sectionTitle = null;
        if (board.getSectionId() != null) {
            sectionTitle = resolveSectionTitle(board.getSectionId(), sectionTitleMap);
        }

        return new BoardViewDTO(
                board.getPostId(),
                board.getMemberId(),
                memberName,
                board.getCourseId(),
                courseTitle,
                courseInstructorId,
                board.getSectionId(),
                sectionTitle,
                board.getTitle(),
                board.getContent(),
                board.getPostType(),
                board.getIsSecret(),
                board.getAnswerStatus(),
                board.getViewCount(),
                board.getIsDeleted(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                profileImage
        );
    }


    private Map<Integer, String> loadMemberNameMap(List<Board> boards) {
        Set<Integer> memberIds = boards.stream()
                .map(Board::getMemberId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getName));
    }

    private Map<Integer, String> loadMemberProfileImageMap(List<Board> boards) {
        Set<Integer> memberIds = boards.stream()
                .map(Board::getMemberId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(
                        Member::getMemberId,
                        member -> member.getProfile() != null ? member.getProfile().getProfileImage() : null
                ));
    }

    private Map<Integer, String> loadCourseTitleMap(List<Board> boards) {
        Set<Integer> courseIds = boards.stream()
                .map(Board::getCourseId)
                .filter(courseId -> courseId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, Course::getTitle));
    }

    private Map<Integer, Integer> loadCourseInstructorMap(List<Board> boards) {
        Set<Integer> courseIds = boards.stream()
                .map(Board::getCourseId)
                .filter(courseId -> courseId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, Course::getInstructorId));
    }

    private Map<Integer, String> loadSectionTitleMap(List<Board> boards) {
        Set<Integer> sectionIds = boards.stream()
                .map(Board::getSectionId)
                .filter(sectionId -> sectionId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (sectionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return sectionRepository.findAllById(sectionIds).stream()
                .collect(Collectors.toMap(Section::getSectionId, Section::getTitle));
    }

    private String resolveMemberName(Integer memberId, Map<Integer, String> memberNameMap) {
        if (memberId == null) {
            throw new IllegalStateException("게시글 작성자 정보가 없습니다.");
        }

        String memberName = memberNameMap.get(memberId);
        if (memberName == null) {
            throw new IllegalStateException("작성자 이름을 찾을 수 없습니다. memberId=" + memberId);
        }

        return memberName;
    }

    private String resolveMemberProfileImage(Integer memberId, Map<Integer, String> memberProfileImageMap) {
        if (memberId == null) {
            return null;
        }

        return memberProfileImageMap.get(memberId);
    }

    private String resolveCourseTitle(Integer courseId, Map<Integer, String> courseTitleMap) {
        String courseTitle = courseTitleMap.get(courseId);
        if (courseTitle == null) {
            throw new IllegalStateException("코스 제목을 찾을 수 없습니다. courseId=" + courseId);
        }

        return courseTitle;
    }

    private Integer resolveCourseInstructorId(Integer courseId, Map<Integer, Integer> courseInstructorMap) {
        Integer courseInstructorId = courseInstructorMap.get(courseId);
        if (courseInstructorId == null) {
            throw new IllegalStateException("코스 강사 정보를 찾을 수 없습니다. courseId=" + courseId);
        }

        return courseInstructorId;
    }

    private String resolveSectionTitle(Integer sectionId, Map<Integer, String> sectionTitleMap) {
        String sectionTitle = sectionTitleMap.get(sectionId);
        if (sectionTitle == null) {
            throw new IllegalStateException("섹션 제목을 찾을 수 없습니다. sectionId=" + sectionId);
        }

        return sectionTitle;
    }

    private String formatSectionQnaTitle(String sectionTitle, String rawTitle) {
        String normalizedTitle = rawTitle == null ? "" : rawTitle.trim();
        String prefix = "[" + sectionTitle + "] ";

        if (normalizedTitle.startsWith("[")) {
            int closingIndex = normalizedTitle.indexOf(']');
            if (closingIndex >= 0 && normalizedTitle.length() > closingIndex + 1) {
                normalizedTitle = normalizedTitle.substring(closingIndex + 1).trim();
            }
        }

        return prefix + normalizedTitle;
    }

    @Transactional
    public void increaseViewCount(Integer postId) {
        Board board = findActiveBoard(postId);
        board.increasedViewCount();
    }

    @Transactional
    public void changeAnswerStatusToAnswered(Integer postId) {
        Board board = findActiveBoard(postId);

        if (board.getPostType() != BoardType.SECTION_QNA) {
            return;
        }
        board.changeAnswerStatus(AnswerStatus.ANSWERED);
    }
}
