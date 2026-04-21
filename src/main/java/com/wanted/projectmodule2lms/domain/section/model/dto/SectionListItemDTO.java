package com.wanted.projectmodule2lms.domain.section.model.dto;

import java.time.LocalDate;

public class SectionListItemDTO {

    private Integer sectionId;
    private String title;
    private Integer sectionOrder;
    private LocalDate openDate;
    private String materialFile;
    private Integer courseId;

    public SectionListItemDTO(Integer sectionId,
                              String title,
                              Integer sectionOrder,
                              LocalDate openDate,
                              String materialFile,
                              Integer courseId) {
        this.sectionId = sectionId;
        this.title = title;
        this.sectionOrder = sectionOrder;
        this.openDate = openDate;
        this.materialFile = materialFile;
        this.courseId = courseId;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public String getTitle() {
        return title;
    }

    public Integer getSectionOrder() {
        return sectionOrder;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public String getMaterialFile() {
        return materialFile;
    }

    public Integer getCourseId() {
        return courseId;
    }
}
