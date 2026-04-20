package com.wanted.projectmodule2lms.domain.grade.model.dao;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeQueryRepository extends JpaRepository<Grade, Integer> {

    @Query("""
        select new com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO(
            g.gradeId,
            g.enrollmentId,
            e.courseId,
            m.name,
            c.title,
            g.attendanceScore,
            g.assignmentScore,
            g.examScore,
            g.attitudeScore,
            g.totalScore,
            case
                when g.isPassed is null then '미채점'
                when g.isPassed = true then '수료'
                else '미수료'
            end
        )
        from Enrollment e
        join Grade g on g.enrollmentId = e.enrollmentId
        join Course c on c.courseId = e.courseId
        join Member m on m.memberId = e.memberId
        where e.memberId = :memberId
    """)
    List<GradeDTO> findGradesByMemberId(@Param("memberId") Integer memberId);

    @Query("""
    select new com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO(
        g.gradeId,
        g.enrollmentId,
        e.courseId,
        m.name,
        c.title,
        g.attendanceScore,
        g.assignmentScore,
        g.examScore,
        g.attitudeScore,
        g.totalScore,
        case
            when g.isPassed is null then '미채점'
            when g.isPassed = true then '수료'
            else '미수료'
        end
    )
    from Enrollment e
    join Grade g on g.enrollmentId = e.enrollmentId
    join Course c on c.courseId = e.courseId
    join Member m on m.memberId = e.memberId
    where c.instructorId = :instructorId
""")
    List<GradeDTO> findGradesByInstructorId(@Param("instructorId") Integer instructorId);


    @Query("""
    select new com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO(
        g.gradeId,
        g.enrollmentId,
        e.courseId,
        m.name,
        c.title,
        g.attendanceScore,
        g.assignmentScore,
        g.examScore,
        g.attitudeScore,
        g.totalScore,
        case
            when g.isPassed is null then '미채점'
            when g.isPassed = true then '수료'
            else '미수료'
        end
    )
    from Enrollment e
    join Grade g on g.enrollmentId = e.enrollmentId
    join Course c on c.courseId = e.courseId
    join Member m on m.memberId = e.memberId
    where e.courseId = :courseId
""")
    List<GradeDTO> findGradesByCourseId(@Param("courseId") Integer courseId);


}
