package com.wanted.projectmodule2lms.domain.member.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    // 승인 메일 보내기
    public void sendApprovalEmail(String toEmail, String approvalCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[64Lab] 강사 가입 승인 안내");
        message.setText("축하드립니다! 강사 가입 신청이 승인되었습니다.\n\n" +
                "로그인 후 아래의 승인 코드를 입력하여 계정을 활성화해주세요.\n" +
                "승인 코드 : " + approvalCode);

        mailSender.send(message);
    }

    // 반려 메일 보내기
    public void sendRejectEmail(String toEmail, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[64Lab] 강사 가입 반려 안내");
        message.setText("안녕하세요. 가입 신청하신 내용에 보완이 필요하여 반려되었습니다.\n\n" +
                "반려 사유 : " + reason + "\n\n" +
                "사유를 확인하신 후 다시 신청해주시기 바랍니다.");

        mailSender.send(message);
    }
}