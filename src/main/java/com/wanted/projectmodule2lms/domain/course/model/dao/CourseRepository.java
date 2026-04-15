package com.wanted.projectmodule2lms.domain.course.model.dao;

import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Integer> {


    List<Course> findByIsOpenTrue();


    List<Course> findByTitleContaining(String keyword);

    List<Course> findByInstructorId(Integer instructorId);

    List<Course> findAllByOrderByCourseIdDesc();

    List<Course> findAllByInstructorIdOrderByCourseIdDesc(Integer instructorId);

    List<Course> findAllByIsOpenTrueOrderByCourseIdDesc();

    List<Course> findByApprovalStatusOrderByCourseIdDesc(CourseApprovalStatus approvalStatus);

    List<Course> findByCourseIdIn(List<Integer> courseIds);
}
