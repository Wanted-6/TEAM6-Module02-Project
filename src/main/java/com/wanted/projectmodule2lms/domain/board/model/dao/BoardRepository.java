package com.wanted.projectmodule2lms.domain.board.model.dao;
import com.wanted.projectmodule2lms.domain.board.model.entity.Board;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findByIsDeletedFalse();

    List<Board> findByPostTypeAndIsDeletedFalse(BoardType postType);

    List<Board> findByTitleContainingAndIsDeletedFalse(String title);

    List<Board> findByPostTypeAndTitleContainingAndIsDeletedFalse(BoardType postType, String title);
}
