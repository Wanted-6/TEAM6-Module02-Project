package com.wanted.projectmodule2lms.domain.course.model.dao;

import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    boolean existsByInstructorIdAndIsOpenTrue(Integer instructorId);

    @Query("""
            select c
            from Course c
            where c.approvalStatus = com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus.APPROVED
              and c.isOpen = true
              and (:keyword is null or c.title like concat('%', :keyword, '%'))
              and (:category is null or c.category = :category)
              and (
                    select count(s.sectionId)
                    from Section s
                    where s.courseId = c.courseId
                  ) = 8
            order by c.courseId desc
            """)
    List<Course> findOpenEnrollmentCourses(String keyword, String category);

    @Query("""
            select c
            from Course c
            where c.courseId in :courseIds
              and c.approvalStatus = com.wanted.projectmodule2lms.domain.course.model.entity.CourseApprovalStatus.APPROVED
              and c.isOpen = true
              and (
                    select count(s.sectionId)
                    from Section s
                    where s.courseId = c.courseId
                  ) = 8
            order by c.courseId desc
            """)
    List<Course> findAvailableCoursesByCourseIds(List<Integer> courseIds);
}



