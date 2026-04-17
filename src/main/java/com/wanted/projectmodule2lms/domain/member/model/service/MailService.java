package com.wanted.projectmodule2lms.domain.member.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public void sendApprovalEmail(String toEmail, String approvalCode) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.info("메일 설정이 없어 승인 메일 전송을 건너뜀. toEmail={}, approvalCode={}", toEmail, approvalCode);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[64Lab] 강사 가입 승인 안내");
        message.setText("축하합니다! 강사 가입 요청이 승인되었습니다.\n\n"
                + "로그인 후 아래 승인 코드를 입력해 계정을 활성화해주세요.\n"
                + "승인 코드 : " + approvalCode);

        mailSender.send(message);
    }

    public void sendRejectEmail(String toEmail, String reason) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.info("메일 설정이 없어 반려 메일 전송을 건너뜀. toEmail={}, reason={}", toEmail, reason);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[64Lab] 강사 가입 반려 안내");
        message.setText("안녕하세요. 강사 가입 요청 내용에 보완이 필요해 반려되었습니다.\n\n"
                + "반려 사유 : " + reason + "\n\n"
                + "사유를 확인한 뒤 다시 요청해주세요.");

        mailSender.send(message);
    }
}
