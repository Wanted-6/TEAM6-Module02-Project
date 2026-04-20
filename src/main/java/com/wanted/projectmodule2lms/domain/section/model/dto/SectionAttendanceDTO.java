package com.wanted.projectmodule2lms.domain.section.model.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class SectionAttendanceDTO {
    private Integer sectionId;
    private String sectionTitle;
    private String status;
}
