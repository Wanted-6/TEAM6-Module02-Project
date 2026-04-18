package com.wanted.projectmodule2lms.domain.auth.config;

import com.wanted.projectmodule2lms.domain.auth.handler.AuthFailHandler;
import com.wanted.projectmodule2lms.domain.auth.handler.AuthSuccessHandler;
import com.wanted.projectmodule2lms.domain.auth.model.service.LoginLogService;
import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public AuthFailHandler authFailHandler(MemberService memberService, LoginLogService loginLogService) {
        return new AuthFailHandler(memberService, loginLogService);
    }

    @Bean
    public AuthSuccessHandler authSuccessHandler(MemberService memberService, LoginLogService loginLogService) {
        return new AuthSuccessHandler(memberService, loginLogService);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http,
                                         AuthFailHandler authFailHandler,
                                         AuthSuccessHandler authSuccessHandler,
                                         SessionRegistry sessionRegistry) throws Exception {

        http.authorizeHttpRequests(auth -> {
                    // 1. 누구나 접근 가능한 URL 설정
                    auth.requestMatchers("/auth/**", "/member/signup", "/", "/api/member/**").permitAll();

                    // 2. 권한별 URL 접근 제어 (HTML에서 설정한 3가지 Role 기준)

                    // 관리자 전용
                    auth.requestMatchers("/auth/**", "/member/signup", "/", "/api/member/**", "/error").permitAll();
                    auth.requestMatchers("/admin/**").hasAnyAuthority("ADMIN");
                    auth.requestMatchers("/instructor/**").hasAnyAuthority("INSTRUCTOR", "ADMIN");
                    auth.requestMatchers("/member/**").hasAnyAuthority("STUDENT", "INSTRUCTOR", "ADMIN");
                    auth.anyRequest().authenticated();
                }).formLogin(login -> {
                    login.loginPage("/auth/login");
                    login.usernameParameter("memberId");
                    login.passwordParameter("pass");
                    login.successHandler(authSuccessHandler);
                    login.failureHandler(authFailHandler);
                }).rememberMe(rememberMe -> {
                    rememberMe.rememberMeParameter("remember-me");
                    rememberMe.tokenValiditySeconds(86400);
                    rememberMe.key("remember-me-secret-key");
                }).logout(logout -> {
                    logout.logoutUrl("/auth/logout");
                    logout.deleteCookies("JSESSIONID");
                    logout.deleteCookies("remember-me");
                    logout.invalidateHttpSession(true);
                    logout.clearAuthentication(true);
                    logout.logoutSuccessUrl("/");
                }).sessionManagement(session -> {
                    session.maximumSessions(1).sessionRegistry(sessionRegistry);
                    session.invalidSessionUrl("/");
                }).exceptionHandling(exception -> exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (!response.isCommitted()) {
                        response.sendRedirect("/");
                    }
                })).csrf(csrf -> csrf.disable());

        return http.build();
    }
}
