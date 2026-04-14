package com.wanted.projectmodule2lms.domain.enrollment.controller;

import com.wanted.projectmodule2lms.domain.enrollment.model.dto.EnrollmentCreateDTO;
import com.wanted.projectmodule2lms.domain.enrollment.model.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<String> enroll(@RequestBody EnrollmentCreateDTO request) {
        enrollmentService.enrollCourse(request.getMemberId(), request.getCourseId());
        return ResponseEntity.ok("수강신청이 완료되었습니다.");
    }
}
