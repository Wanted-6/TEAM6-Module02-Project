package com.wanted.projectmodule2lms.domain.section.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "Section")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "material_file", length = 255)
    private String materialFile;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Column(name = "open_date")
    private LocalDate openDate;

    public void changeSectionInfo(String title,
                                  String videoUrl,
                                  String materialFile,
                                  Integer sectionOrder,
                                  LocalDate openDate) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.materialFile = materialFile;
        this.sectionOrder = sectionOrder;
        this.openDate = openDate;
    }
}
