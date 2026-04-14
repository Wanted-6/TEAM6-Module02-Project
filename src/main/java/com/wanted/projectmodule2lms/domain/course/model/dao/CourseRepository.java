package com.wanted.projectmodule2lms.domain.course.model.dao;

import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByIsOpenTrue();

    List<Course> findByTitleContaining(String keyword);

    List<Course> findByInstructorId(Integer instructorId);

    List<Course> findByCategoryAndIsOpenTrue(String category);
}