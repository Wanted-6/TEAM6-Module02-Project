package com.wanted.projectmodule2lms.domain.certificate.model.service;

import com.wanted.projectmodule2lms.domain.attendance.model.service.AttendanceService;
import com.wanted.projectmodule2lms.domain.certificate.model.dao.CertificateRepository;
import com.wanted.projectmodule2lms.domain.certificate.model.dto.CertificateAdminDTO;
import com.wanted.projectmodule2lms.domain.certificate.model.dto.CertificateViewDTO;
import com.wanted.projectmodule2lms.domain.certificate.model.entity.Certificate;
import com.wanted.projectmodule2lms.domain.certificate.model.entity.CertificateStatus;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateService {

    private static final DateTimeFormatter CERTIFICATE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceService attendanceService;
    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public void requestCertificate(Integer memberId, Integer courseId) {
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스가 아닙니다."));

        Certificate foundCertificate = certificateRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                .orElse(null);

        if (foundCertificate != null) {
            throw new IllegalArgumentException("이미 수료증 신청 이력이 있습니다.");
        }

        Integer totalScore = attendanceService.calculateTotalScore(memberId, courseId);

        if (totalScore < 80) {
            throw new IllegalArgumentException("수료 기준을 충족하지 못했습니다.");
        }

        Certificate certificate = new Certificate(
                enrollment.getEnrollmentId(),
                BigDecimal.valueOf(totalScore)
        );

        certificateRepository.save(certificate);
    }

    public List<CertificateAdminDTO> findAllCertificatesForAdmin() {
        List<Certificate> certificateList = certificateRepository.findAllByOrderByRequestedAtDesc();
        List<CertificateAdminDTO> certificateAdminDTOList = new ArrayList<>();

        for (Certificate certificate : certificateList) {
            Enrollment enrollment = enrollmentRepository.findById(certificate.getEnrollmentId())
                    .orElse(null);

            if (enrollment == null) {
                continue;
            }

            Member member = memberRepository.findById(enrollment.getMemberId())
                    .orElse(null);

            Course course = courseRepository.findById(enrollment.getCourseId())
                    .orElse(null);

            if (member == null || course == null) {
                continue;
            }

            CertificateAdminDTO dto = new CertificateAdminDTO(
                    certificate.getCertificateId(),
                    enrollment.getEnrollmentId(),
                    member.getMemberId(),
                    course.getCourseId(),
                    member.getName(),
                    member.getEmail(),
                    course.getTitle(),
                    certificate.getStatus().name(),
                    certificate.getTotalScore(),
                    certificate.getRequestedAt()
            );

            certificateAdminDTOList.add(dto);
        }

        return certificateAdminDTOList;
    }

    @Transactional
    public void approveCertificate(Integer certificateId, Integer adminId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("수료증 신청이 없습니다."));

        if (certificate.getStatus() != CertificateStatus.REQUESTED) {
            throw new IllegalArgumentException("승인 가능한 상태가 아닙니다.");
        }

        certificate.approve(adminId);
    }

    public CertificateViewDTO findCertificateForStudent(Integer memberId, Integer courseId) {
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스를 찾을 수 없습니다."));

        Certificate certificate = certificateRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 코스의 수료증 정보를 찾을 수 없습니다."));

        if (certificate.getStatus() != CertificateStatus.APPROVED
                && certificate.getStatus() != CertificateStatus.ISSUED) {
            throw new IllegalArgumentException("발급 가능한 수료증이 아닙니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스 정보를 찾을 수 없습니다."));

        return new CertificateViewDTO(
                member.getName(),
                course.getTitle(),
                certificate.getTotalScore(),
                certificate.getStatus().name(),
                certificate.getApprovedAt()
        );
    }

    @Transactional
    public byte[] generateCertificatePdf(Integer memberId, Integer courseId) {
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 코스를 찾을 수 없습니다."));
        Certificate issuedCertificate = certificateRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 코스의 수료증 정보를 찾을 수 없습니다."));
        CertificateViewDTO certificate = findCertificateForStudent(memberId, courseId);
        Path fontPath = Path.of(System.getenv("WINDIR"), "Fonts", "malgun.ttf");

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, fontPath.toFile());

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float currentY = 720f;

                writeCenteredText(contentStream, font, 28, pageWidth, currentY, "수료증");
                currentY -= 50f;
                writeCenteredText(contentStream, font, 14, pageWidth, currentY,
                        "아래 학생은 과정을 성실히 이수하였으므로 본 증서를 수여합니다.");

                currentY -= 90f;
                writeCenteredText(contentStream, font, 24, pageWidth, currentY, certificate.getStudentName());

                currentY -= 70f;
                writeText(contentStream, font, 14, 120f, currentY,
                        "과정명: " + certificate.getCourseTitle());
                currentY -= 30f;
                writeText(contentStream, font, 14, 120f, currentY,
                        "총점: " + certificate.getTotalScore() + "점");
                currentY -= 30f;
                writeText(contentStream, font, 14, 120f, currentY, "수료 여부: 수료");
                currentY -= 30f;
                writeText(contentStream, font, 14, 120f, currentY,
                        "승인일: " + formatDateTime(certificate.getApprovedAt()));
            }

            document.save(outputStream);

            if (issuedCertificate.getStatus() == CertificateStatus.APPROVED) {
                issuedCertificate.issue(null);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("수료증 PDF 생성에 실패했습니다.", e);
        }
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(CERTIFICATE_DATE_FORMAT);
    }

    private void writeCenteredText(PDPageContentStream contentStream,
                                   PDType0Font font,
                                   float fontSize,
                                   float pageWidth,
                                   float y,
                                   String text) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float startX = (pageWidth - textWidth) / 2;
        writeText(contentStream, font, fontSize, startX, y, text);
    }

    private void writeText(PDPageContentStream contentStream,
                           PDType0Font font,
                           float fontSize,
                           float x,
                           float y,
                           String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    public Integer findFirstSectionId(Integer courseId){
        Section section = sectionRepository.findByCourseIdAndSectionOrder(courseId, 1)
                .orElseGet(() -> {
                    List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);
                    if (sectionList.isEmpty()) {
                        return null;
                    }
                    return sectionList.get(0);
                });
        return section != null ? section.getSectionId() : null;
    }

}
