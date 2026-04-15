package com.wanted.projectmodule2lms.domain.course.model.dao;

import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Integer> {

<<<<<<< HEAD
=======

    List<Course> findByIsOpenTrue();


    List<Course> findByTitleContaining(String keyword);

    List<Course> findByInstructorId(Integer instructorId);

>>>>>>> e2e9153072af011967b7c7dc2b13480a9a8a3091
    List<Course> findAllByOrderByCourseIdDesc();

    List<Course> findAllByInstructorIdOrderByCourseIdDesc(Integer instructorId);

    List<Course> findAllByIsOpenTrueOrderByCourseIdDesc();

    List<Course> findByApprovalStatusOrderByCourseIdDesc(CourseApprovalStatus approvalStatus);
<<<<<<< HEAD

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
}
=======
}
>>>>>>> e2e9153072af011967b7c7dc2b13480a9a8a3091
