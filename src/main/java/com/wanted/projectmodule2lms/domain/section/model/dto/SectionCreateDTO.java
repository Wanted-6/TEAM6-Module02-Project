package com.wanted.projectmodule2lms.domain.section.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SectionCreateDTO {

    private Integer sectionId;
    private String title;
    private String videoUrl;
    private String materialFile;
    private Integer sectionOrder;
    private LocalDate openDate;
}