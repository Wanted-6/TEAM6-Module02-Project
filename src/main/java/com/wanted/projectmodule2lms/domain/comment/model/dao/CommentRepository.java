package com.wanted.projectmodule2lms.domain.comment.model.dao;


import com.wanted.projectmodule2lms.domain.comment.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPostIdAndIsDeletedFalse(Integer postId);

    List<Comment> findByPostIdAndIsDeletedFalseOrderByCommentIdAsc(Integer postId);
}
