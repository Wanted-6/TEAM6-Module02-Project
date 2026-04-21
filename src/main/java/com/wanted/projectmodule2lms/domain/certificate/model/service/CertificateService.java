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
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedStudentAccessException;
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
                .orElseThrow(() -> new UnauthorizedStudentAccessException("??띿뺏 餓λ쵐???꾨뗄?ゅ첎? ?袁⑤뻸??덈뼄."));

        Certificate foundCertificate = certificateRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                .orElse(null);

        if (foundCertificate != null) {
            throw new IllegalArgumentException("???? ??濡?뵹嶺???ル―???????????곕????덈펲.");
        }

        Integer totalScore = attendanceService.calculateTotalScore(memberId, courseId);

        if (totalScore < 80) {
            throw new IllegalArgumentException("??濡?뵹 ?リ옇?????寃몃쳴???? 嶺뚮쪇沅?쭛???鍮??");
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
                .orElseThrow(() -> new ResourceNotFoundException("??롮┷筌??醫롪퍕????곷뮸??덈뼄."));

        if (certificate.getStatus() != CertificateStatus.REQUESTED) {
            throw new IllegalArgumentException("?獄????띠럾??繞③뇡???⑤객臾뜻뤆?쎛 ?熬곣뫀六???덈펲.");
        }

        certificate.approve(adminId);
    }

    public CertificateViewDTO findCertificateForStudent(Integer memberId, Integer courseId) {
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new UnauthorizedStudentAccessException("??띿뺏 餓λ쵐???꾨뗄?ょ몴?筌≪뼚??????곷뮸??덈뼄."));

        Certificate certificate = certificateRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("?????꾨뗄?????롮┷筌??類ｋ궖??筌≪뼚??????곷뮸??덈뼄."));

        if (certificate.getStatus() != CertificateStatus.APPROVED
                && certificate.getStatus() != CertificateStatus.ISSUED) {
            throw new IllegalArgumentException("?꾩룇裕???띠럾??繞③뇡???濡?뵹嶺뚯빘鍮???熬곣뫀六???덈펲.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("??덇문 ?類ｋ궖??筌≪뼚??????곷뮸??덈뼄."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("?꾨뗄???類ｋ궖??筌≪뼚??????곷뮸??덈뼄."));

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
                .orElseThrow(() -> new UnauthorizedStudentAccessException("??띿뺏 餓λ쵐???꾨뗄?ょ몴?筌≪뼚??????곷뮸??덈뼄."));
        Certificate issuedCertificate = certificateRepository.findByEnrollmentId(enrollment.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("?????꾨뗄?????롮┷筌??類ｋ궖??筌≪뼚??????곷뮸??덈뼄."));
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
                        "?熬곣뫁?????뉖Ц?? ??λ닔????繹먮끏堉????怨룸빢??????????嶺뚯빘鍮섋땻?⑤ご???琉우뿰??紐껊퉵??");

                currentY -= 90f;
                writeCenteredText(contentStream, font, 24, pageWidth, currentY, certificate.getStudentName());

                currentY -= 70f;
                writeText(contentStream, font, 14, 120f, currentY,
                        "??λ닔??뗭춻? " + certificate.getCourseTitle());
                currentY -= 30f;
                writeText(contentStream, font, 14, 120f, currentY,
                        "총점: " + certificate.getTotalScore() + "점");
                currentY -= 30f;
                writeText(contentStream, font, 14, 120f, currentY, "??濡?뵹 ???: ??濡?뵹");
                currentY -= 30f;
                writeText(contentStream, font, 14, 120f, currentY,
                        "?獄???? " + formatDateTime(certificate.getApprovedAt()));
            }

            document.save(outputStream);

            if (issuedCertificate.getStatus() == CertificateStatus.APPROVED) {
                issuedCertificate.issue(null);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("??濡?뵹嶺?PDF ??諛댁뎽?????덉넮???곕????덈펲.", e);
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
