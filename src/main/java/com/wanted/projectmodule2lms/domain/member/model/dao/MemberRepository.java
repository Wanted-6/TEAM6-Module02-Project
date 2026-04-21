package com.wanted.projectmodule2lms.domain.member.model.dao;

import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByPhone(String phone);

    boolean existsByLoginId(String loginId);

    boolean existsByPhone(String phone);


    // 아이디 찾기
    Optional<Member> findByNameAndEmail(String name, String email);

    // 비밀번호 찾기
    Optional<Member> findByLoginIdAndEmail(String loginId, String email);

    List<Member> findByMemberIdIn(List<Integer> memberIds);

    // /admin/instructor 성능 개선 부분.
    //    List<Member> findByRole(MemberRole role);
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.profile WHERE m.role = :role")
    List<Member> findByRole(@Param("role") MemberRole role);
}
