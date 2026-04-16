package com.wanted.projectmodule2lms.domain.board.model.service;

import com.wanted.projectmodule2lms.domain.board.model.dao.BoardRepository;
import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
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
public class BoardService {

    private final BoardRepository boardRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final MemberRepository memberRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;

    public List<BoardDTO> findBoardByPostType(BoardType postType) {
        return toBoardDTOList(boardRepository.findByPostTypeAndIsDeletedFalse(postType));
    }

    public List<BoardDTO> searchBoardByTitle(String title) {
        return toBoardDTOList(boardRepository.findByTitleContainingAndIsDeletedFalse(title));
    }

    public List<BoardDTO> searchBoardByPostTypeAndTitle(BoardType postType, String title) {
        return toBoardDTOList(boardRepository.findByPostTypeAndTitleContainingAndIsDeletedFalse(postType, title));
    }

    public List<BoardDTO> findVisibleSectionQna(Integer currentMemberId, MemberRole currentRole, String keyword) {
        List<Board> boards = (keyword == null || keyword.isBlank())
                ? boardRepository.findByPostTypeAndIsDeletedFalse(BoardType.SECTION_QNA)
                : boardRepository.findByPostTypeAndTitleContainingAndIsDeletedFalse(BoardType.SECTION_QNA, keyword);

        Set<Integer> allowedCourseIds = getAllowedCourseIdsForSectionQna(currentMemberId, currentRole);

        return toBoardDTOList(boards.stream()
                .filter(board -> board.getCourseId() != null && allowedCourseIds.contains(board.getCourseId()))
                .collect(Collectors.toList()));
    }

    public BoardDTO findBoardById(Integer postId) {
        return toBoardDTO(findActiveBoard(postId));
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
            throw new IllegalArgumentException("?대떦 寃뚯떆?먯뿉 湲???깅줉??沅뚰븳???놁뒿?덈떎.");
        }

        normalizeBoardDTO(boardDTO, currentMemberId, currentRole);

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
        Board board = findActiveBoard(boardDTO.getPostId());

        if (!canModifyOrDelete(currentMemberId, currentRole, board)) {
            throw new IllegalArgumentException("?섏젙 沅뚰븳???놁뒿?덈떎.");
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
            throw new IllegalArgumentException("??젣 沅뚰븳???놁뒿?덈떎.");
        }

        board.deleteBoard();
    }

    public Board findActiveBoard(Integer postId) {
        return boardRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("?대떦 寃뚯떆湲??議댁옱?섏? ?딄굅????젣?섏뿀?듬땲??"));
    }

    private void normalizeBoardDTO(BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
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
                throw new IllegalArgumentException("肄붿뒪 怨듭???肄붿뒪瑜??좏깮?댁빞 ?⑸땲??");
            }
            if (!isInstructorCourse(boardDTO.getCourseId(), currentMemberId)) {
                throw new IllegalArgumentException("?꾩옱 媛뺤궗媛 ?대떦?섎뒗 肄붿뒪留?怨듭? ?깅줉??媛?ν빀?덈떎.");
            }
            boardDTO.setSectionId(null);
            boardDTO.setIsSecret(false);
            boardDTO.setAnswerStatus(null);
            return;
        }

        validateSectionQnaAccess(boardDTO.getCourseId(), boardDTO.getSectionId(), currentMemberId, currentRole, true);
        Section section = sectionRepository.findById(boardDTO.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 섹션을 선택해야 합니다."));
        boardDTO.setTitle(formatSectionQnaTitle(section.getTitle(), boardDTO.getTitle()));
        boardDTO.setIsSecret(Boolean.TRUE.equals(boardDTO.getIsSecret()));
        boardDTO.setAnswerStatus(AnswerStatus.PENDING);
    }

    private boolean needsCourseId(BoardType postType) {
        return postType == BoardType.COURSE_NOTICE || postType == BoardType.SECTION_QNA;
    }

    private Integer resolveModifiedCourseId(Board board, BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        if (!needsCourseId(board.getPostType())) {
            return null;
        }

        if (boardDTO.getCourseId() == null) {
            throw new IllegalArgumentException("肄붿뒪 ?뺣낫媛 ?꾩슂??寃뚯떆湲?낅땲??");
        }

        if (board.getPostType() == BoardType.COURSE_NOTICE && !isInstructorCourse(boardDTO.getCourseId(), currentMemberId)) {
            throw new IllegalArgumentException("?꾩옱 媛뺤궗媛 ?대떦?섎뒗 肄붿뒪留?怨듭? ?섏젙??媛?ν빀?덈떎.");
        }

        if (board.getPostType() == BoardType.SECTION_QNA) {
            validateSectionQnaAccess(boardDTO.getCourseId(), boardDTO.getSectionId(), currentMemberId, currentRole, false);
        }

        if (board.getPostType() == BoardType.SECTION_QNA && !boardDTO.getCourseId().equals(board.getCourseId())) {
            throw new IllegalArgumentException("?뱀뀡 Q&A??肄붿뒪 ?뺣낫瑜?蹂寃쏀븷 ???놁뒿?덈떎.");
        }

        return boardDTO.getCourseId();
    }

    private Integer resolveModifiedSectionId(Board board, BoardDTO boardDTO, Integer currentMemberId, MemberRole currentRole) {
        if (board.getPostType() != BoardType.SECTION_QNA) {
            return null;
        }

        if (boardDTO.getSectionId() == null) {
            throw new IllegalArgumentException("?뱀뀡 Q&A???뱀뀡 ?뺣낫瑜??낅젰?댁빞 ?⑸땲??");
        }

        validateSectionQnaAccess(board.getCourseId(), boardDTO.getSectionId(), currentMemberId, currentRole, false);

        if (!boardDTO.getSectionId().equals(board.getSectionId())) {
            throw new IllegalArgumentException("?뱀뀡 Q&A???뱀뀡 ?뺣낫瑜?蹂寃쏀븷 ???놁뒿?덈떎.");
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
            throw new IllegalArgumentException("?뱀뀡 Q&A??肄붿뒪? ?뱀뀡 ?뺣낫瑜?紐⑤몢 ?낅젰?댁빞 ?⑸땲??");
        }

        Set<Integer> allowedCourseIds = getAllowedCourseIdsForSectionQna(currentMemberId, currentRole);
        if (!allowedCourseIds.contains(courseId)) {
            throw new IllegalArgumentException(isCreate
                    ? "?꾩옱 ?ъ슜?먭? ?묎렐 媛?ν븳 肄붿뒪?먯꽌留?吏덈Ц???깅줉?????덉뒿?덈떎."
                    : "?꾩옱 ?ъ슜?먭? ?묎렐 媛?ν븳 肄붿뒪??吏덈Ц留??섏젙?????덉뒿?덈떎.");
        }

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("?좏슚???뱀뀡???좏깮?댁빞 ?⑸땲??"));

        if (!section.getCourseId().equals(courseId)) {
            throw new IllegalArgumentException("?좏깮???뱀뀡? ?대떦 肄붿뒪???랁븯吏 ?딆뒿?덈떎.");
        }
    }

    private BoardDTO toBoardDTO(Board board) {
        return toBoardDTO(
                board,
                loadMemberNameMap(Collections.singletonList(board)),
                loadCourseTitleMap(Collections.singletonList(board)),
                loadCourseInstructorMap(Collections.singletonList(board)),
                loadSectionTitleMap(Collections.singletonList(board))
        );
    }

    private List<BoardDTO> toBoardDTOList(List<Board> boards) {
        Map<Integer, String> memberNameMap = loadMemberNameMap(boards);
        Map<Integer, String> courseTitleMap = loadCourseTitleMap(boards);
        Map<Integer, Integer> courseInstructorMap = loadCourseInstructorMap(boards);
        Map<Integer, String> sectionTitleMap = loadSectionTitleMap(boards);

        return boards.stream()
                .map(board -> toBoardDTO(board, memberNameMap, courseTitleMap, courseInstructorMap, sectionTitleMap))
                .collect(Collectors.toList());
    }

    private BoardDTO toBoardDTO(Board board,
                                Map<Integer, String> memberNameMap,
                                Map<Integer, String> courseTitleMap,
                                Map<Integer, Integer> courseInstructorMap,
                                Map<Integer, String> sectionTitleMap) {
        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);
        boardDTO.setMemberName(resolveMemberName(board.getMemberId(), memberNameMap));

        if (board.getCourseId() != null) {
            boardDTO.setCourseTitle(resolveCourseTitle(board.getCourseId(), courseTitleMap));
            boardDTO.setCourseInstructorId(resolveCourseInstructorId(board.getCourseId(), courseInstructorMap));
        }

        if (board.getSectionId() != null) {
            boardDTO.setSectionTitle(resolveSectionTitle(board.getSectionId(), sectionTitleMap));
        }

        return boardDTO;
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
            throw new IllegalStateException("寃뚯떆湲 ?묒꽦???뺣낫媛 鍮꾩뼱 ?덉뒿?덈떎.");
        }

        String memberName = memberNameMap.get(memberId);
        if (memberName == null) {
            throw new IllegalStateException("寃뚯떆湲 ?묒꽦???뺣낫瑜?李얠쓣 ???놁뒿?덈떎. memberId=" + memberId);
        }

        return memberName;
    }

    private String resolveCourseTitle(Integer courseId, Map<Integer, String> courseTitleMap) {
        String courseTitle = courseTitleMap.get(courseId);
        if (courseTitle == null) {
            throw new IllegalStateException("寃뚯떆湲 肄붿뒪 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎. courseId=" + courseId);
        }

        return courseTitle;
    }

    private Integer resolveCourseInstructorId(Integer courseId, Map<Integer, Integer> courseInstructorMap) {
        Integer courseInstructorId = courseInstructorMap.get(courseId);
        if (courseInstructorId == null) {
            throw new IllegalStateException("寃뚯떆湲 肄붿뒪 ?대떦 媛뺤궗 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎. courseId=" + courseId);
        }

        return courseInstructorId;
    }

    private String resolveSectionTitle(Integer sectionId, Map<Integer, String> sectionTitleMap) {
        String sectionTitle = sectionTitleMap.get(sectionId);
        if (sectionTitle == null) {
            throw new IllegalStateException("寃뚯떆湲 ?뱀뀡 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎. sectionId=" + sectionId);
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
        Board boad = findActiveBoard(postId);
        boad.increasedViewCount();
    }
 }
