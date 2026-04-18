package com.wanted.projectmodule2lms.domain.member.model.dao;

import com.wanted.projectmodule2lms.domain.member.model.entity.ApprovalStatus;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    // loginId 사용하도록 수정
    Optional<Member> findByLoginId(String loginId);

    // ID, Email, Phone이 이미 존재하는지 확인 (중복 체크)
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);


    // 아이디 찾기
    Optional<Member> findByNameAndEmail(String name, String email);

    // 비밀번호 찾기
    Optional<Member> findByLoginIdAndEmail(String loginId, String email);


    List<Member> findByMemberIdIn(List<Integer> memberIds);

    List<Member> findByRole(MemberRole role);
}
