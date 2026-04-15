package com.wanted.projectmodule2lms.domain.section.model.dao;

import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Integer> {

    List<Section> findByCourseIdInOrderByCourseIdAscSectionOrderAsc(Collection<Integer> courseIds);
    List<Section> findByCourseIdOrderBySectionOrderAsc(Integer courseId);

    boolean existsByCourseIdAndSectionOrder(Integer courseId, Integer sectionOrder);

    boolean existsByCourseIdAndSectionOrderAndSectionIdNot(Integer courseId,
                                                           Integer sectionOrder,
                                                           Integer sectionId);
<<<<<<< HEAD

    long countByCourseId(Integer courseId);

}
=======
}
>>>>>>> e2e9153072af011967b7c7dc2b13480a9a8a3091
