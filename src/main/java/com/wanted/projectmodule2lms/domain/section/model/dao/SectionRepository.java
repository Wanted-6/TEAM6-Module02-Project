package com.wanted.projectmodule2lms.domain.section.model.dao;

import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Integer> {

    List<Section> findByCourseIdInOrderByCourseIdAscSectionOrderAsc(Collection<Integer> courseIds);
    List<Section> findByCourseIdOrderBySectionOrderAsc(Integer courseId);

    boolean existsByCourseIdAndSectionOrder(Integer courseId, Integer sectionOrder);

    boolean existsByCourseIdAndSectionOrderAndSectionIdNot(Integer courseId,
                                                           Integer sectionOrder,
                                                           Integer sectionId);
    List<Section> findByCourseIdIn(List<Integer> courseIds);


    long countByCourseId(Integer courseId);

    Optional<Section> findByCourseIdAndSectionOrder(Integer courseId, Integer sectionOrder);

}



