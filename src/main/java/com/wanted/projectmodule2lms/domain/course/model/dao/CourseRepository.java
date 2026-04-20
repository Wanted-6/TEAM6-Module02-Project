package com.wanted.projectmodule2lms.domain.course.model.dao;

import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByIsOpenTrue();

    List<Course> findByTitleContaining(String keyword);

    List<Course> findByInstructorId(Integer instructorId);

    List<Course> findAllByOrderByCourseIdDesc();

    List<Course> findAllByInstructorIdOrderByCourseIdDesc(Integer instructorId);

    List<Course> findAllByIsOpenTrueOrderByCourseIdDesc();

    List<Course> findByApprovalStatusOrderByCourseIdDesc(CourseApprovalStatus approvalStatus);

    List<Course> findByCourseIdIn(List<Integer> courseIds);

    List<Course> findByApprovalStatusNotOrderByCourseIdDesc(CourseApprovalStatus approvalStatus);

    List<Course> findByApprovalStatusNotAndTitleContainingOrderByCourseIdDesc(CourseApprovalStatus approvalStatus,
                                                                              String keyword);

    List<Course> findByApprovalStatusNotAndCategoryContainingOrderByCourseIdDesc(CourseApprovalStatus approvalStatus,
                                                                                 String category);

    List<Course> findByApprovalStatusNotAndTitleContainingAndCategoryContainingOrderByCourseIdDesc(
            CourseApprovalStatus approvalStatus,
            String keyword,
            String category
    );

    List<Course> findByApprovalStatusAndIsOpenTrueOrderByCourseIdDesc(CourseApprovalStatus approvalStatus);

    List<Course> findByInstructorIdAndApprovalStatusNotOrderByCourseIdDesc(Integer instructorId,
                                                                           CourseApprovalStatus approvalStatus);

    List<Course> findByInstructorIdAndApprovalStatusNotAndTitleContainingOrderByCourseIdDesc(Integer instructorId,
                                                                                             CourseApprovalStatus approvalStatus,
                                                                                             String keyword);

    List<Course> findByInstructorIdAndApprovalStatusNotAndCategoryOrderByCourseIdDesc(Integer instructorId,
                                                                                      CourseApprovalStatus approvalStatus,
                                                                                      String category);

    List<Course> findByInstructorIdAndApprovalStatusNotAndTitleContainingAndCategoryOrderByCourseIdDesc(
            Integer instructorId,
            CourseApprovalStatus approvalStatus,
            String keyword,
            String category
    );

    boolean existsByInstructorIdAndIsOpenTrue(Integer instructorId);
}
