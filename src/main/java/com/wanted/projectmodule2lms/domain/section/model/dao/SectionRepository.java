package com.wanted.projectmodule2lms.domain.section.model.dao;

import com.wanted.projectmodule2lms.domain.section.model.dto.SectionListItemDTO;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        select new com.wanted.projectmodule2lms.domain.section.model.dto.SectionListItemDTO(
            s.sectionId,
            s.title,
            s.sectionOrder,
            s.openDate,
            s.materialFile,
            s.courseId
        )
        from Section s
        where s.courseId = :courseId
        order by s.sectionOrder asc
    """)
    List<SectionListItemDTO> findSectionListByCourseId(@Param("courseId") Integer courseId);
}
