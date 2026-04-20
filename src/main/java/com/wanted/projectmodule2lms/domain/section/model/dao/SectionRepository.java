package com.wanted.projectmodule2lms.domain.section.model.dao;

import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Integer> {

    List<Section> findByCourseIdOrderBySectionOrderAsc(Integer courseId);

    List<Section> findByCourseIdIn(Iterable<Integer> courseIds);

    List<Section> findByCourseIdInOrderByCourseIdAscSectionOrderAsc(Iterable<Integer> courseIds);

    boolean existsByCourseIdAndSectionOrder(Integer courseId, Integer sectionOrder);

    boolean existsByCourseIdAndSectionOrderAndSectionIdNot(Integer courseId, Integer sectionOrder, Integer sectionId);

    boolean existsByCourseIdAndOpenDate(Integer courseId, LocalDate openDate);

    boolean existsByCourseIdAndOpenDateAndSectionIdNot(Integer courseId, LocalDate openDate, Integer sectionId);

    long countByCourseId(Integer courseId);

    Optional<Section> findByCourseIdAndSectionOrder(Integer courseId, Integer sectionOrder);
}
