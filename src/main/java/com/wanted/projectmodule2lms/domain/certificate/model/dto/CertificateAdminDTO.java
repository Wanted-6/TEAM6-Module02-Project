package com.wanted.projectmodule2lms.domain.certificate.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CertificateAdminDTO {

    private Integer certificateId;
    private Integer enrollmentId;
    private Integer memberId;
    private Integer courseId;
    private String studentName;
    private String studentEmail;
    private String courseTitle;
    private String status;
    private BigDecimal totalScore;
    private LocalDateTime requestedAt;
}
