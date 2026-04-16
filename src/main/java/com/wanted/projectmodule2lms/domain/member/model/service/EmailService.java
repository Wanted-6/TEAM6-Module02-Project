package com.wanted.projectmodule2lms.domain.member.model.service;//package com.wanted.security.domain.member.model.service;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender javaMailSender;     //yaml 추가하기
//
//    /**
//     * 이메일 전송 도우미 메서드
//     * @param to 메일 받을 사람의 주소 (수신자)
//     * @param subject 메일 제목
//     * @param text 메일 내용
//     */
//    public void sendEmail(String to, String subject, String text) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(to);            // 받는 이
//            message.setSubject(subject); // 제목
//            message.setText(text);       // 내용
//
//            javaMailSender.send(message); // 전송
//
//            log.info("이메일 전송 성공! (수신자: {})", to);
//        } catch (Exception e) {
//            log.error("이메일 전송 실패... (수신자: {})", to, e);
//            throw new RuntimeException("이메일 전송에 실패했습니다.");
//        }
//    }
//}