package com.wanted.projectmodule2lms.domain.certificate.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CertificateViewDTO {
    private String studentName;
    private String courseTitle;
    private BigDecimal totalScore;
    private String status;
    private LocalDateTime approvedAt;
}
