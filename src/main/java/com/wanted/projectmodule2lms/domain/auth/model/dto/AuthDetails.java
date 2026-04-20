package com.wanted.projectmodule2lms.domain.auth.model.dto;

import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class AuthDetails implements UserDetails {

    private LoginMemberDTO loginMemberDTO;

    public AuthDetails() { }

    public AuthDetails(LoginMemberDTO loginMemberDTO) {
        this.loginMemberDTO = loginMemberDTO;
    }

    public LoginMemberDTO getLoginMemberDTO() {
        return loginMemberDTO;
    }


    public void setLoginMemberDTO(LoginMemberDTO loginMemberDTO) {
        this.loginMemberDTO = loginMemberDTO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        loginMemberDTO.getRoleList().forEach(role -> authorities.add(() -> role));

        return authorities;
    }

    @Override
    public String getPassword() {
        return loginMemberDTO.getPassword();
    }

    @Override
    public String getUsername() {
         return loginMemberDTO.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !loginMemberDTO.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthDetails that)) return false;

        return Objects.equals(this.getUsername(), that.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getUsername());
    }
}
