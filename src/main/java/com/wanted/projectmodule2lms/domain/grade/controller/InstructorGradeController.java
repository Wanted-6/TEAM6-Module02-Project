package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeUpdateDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructor/grades")
@RequiredArgsConstructor
public class InstructorGradeController {

    private final GradeService gradeService;

    // 강사가 담당한 강의의 성적 조회
//    @GetMapping
//    public List<GradeDTO> findGradesByInstructor() {
//        Integer instructorId = 12; // 로그인 가정
//        return gradeService.findGradesByInstructorId(instructorId);
//    }

    // 성적 등록 및 수정 (하나의 기능으로 처리)
//    @PutMapping
//    public String updateGrade(@RequestBody GradeUpdateDTO dto) {
//        Integer instructorId = 12; // 로그인 가정
//        gradeService.updateGradeByInstructor(instructorId, dto);
//        return "성적 등록/수정이 완료되었습니다.";
//    }
}
