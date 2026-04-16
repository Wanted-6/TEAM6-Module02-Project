package com.wanted.projectmodule2lms.domain.attendance.model.dao;

import com.wanted.projectmodule2lms.domain.attendance.model.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    Optional<Attendance> findByEnrollmentIdAndSectionId(Integer enrollmentId, Integer sectionId);

    List<Attendance> findBySectionIdOrderByEnrollmentIdAsc(Integer sectionId);

    List<Attendance> findByEnrollmentId(Integer enrollmentId);

}
