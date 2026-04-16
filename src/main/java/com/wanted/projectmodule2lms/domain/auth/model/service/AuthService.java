package com.wanted.projectmodule2lms.domain.auth.model.service;

import com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * security에서 사용자의 아이디를 인증하기 위한 interface이다.
 * loadUserByUsername을 필수로 구현해야 하며 로그인 인증 시 해당 메서드에
 * login 요청 시 전달된 사용자의 id를 매개변수로 DB에서 조회한다.
 * */

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final MemberService memberService;

    /**
     * AuthenticationProvider에서 호출하는 메서드로
     * login 요청 시 전달된 사용자의 id를 매개변수로 DB에서 사용자의 정보를 찾는다.
     * 전달된 사용자의 개체 타입은 UserDetails를 구현한 구현체가 되어야 한다.
     * */
    @Override                                   //username = ID = 로그인을 위한 메서드
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginMemberDTO login = memberService.findByUsername(username);

        if(Objects.isNull(login)){
            throw new UsernameNotFoundException("회원정보가 존재하지 않습니다.");
        }

        return new AuthDetails(login);
        // (가장 중요한 것은 이 return 이 어디로 가는가. null 이 아니라면 이 return 이 동작함.)
        // 해당 리턴구문이 동작을 하게 되면
        // Security Context 에 LoginDTO 정보를 담은 Session 인증 객체가 만들어지며
        // 우리는 해당 값을 Session 이 유지되는 동안 계속 사용할 수 있게 된다.
    }
}
