package com.wanted.projectmodule2lms.domain.section.model.dao;

import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Integer> {

    List<Section> findByCourseIdOrderBySectionOrderAsc(Integer courseId);

    boolean existsByCourseIdAndSectionOrder(Integer courseId, Integer sectionOrder);

    boolean existsByCourseIdAndSectionOrderAndSectionIdNot(Integer courseId,
                                                           Integer sectionOrder,
                                                           Integer sectionId);

    long countByCourseId(Integer courseId);

}