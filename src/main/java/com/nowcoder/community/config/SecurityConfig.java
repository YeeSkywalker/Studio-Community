package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig implements CommunityConstant {
    @Bean
    public SecurityContextRepository securityContextRepository () {
        return new HttpSessionSecurityContextRepository();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer () {
        return (web) -> web.ignoring().requestMatchers("/resources/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests((authz) -> authz.requestMatchers(
                "/user/setting",
                "/user/upload",
                "/discuss/add",
                "/comment/add/**",
                "/dm/**",
                "/notice/**",
                "/like",
                "/follow",
                "/unfollow"
            ).hasAnyAuthority(
                    AUTHORITY_ADMIN,
                    AUTHORITY_MODERATOR,
                    AUTHORITY_USER
            ).requestMatchers(
                   "/discuss/top",
                "/discuss/wonderful"
            ).hasAnyAuthority(
                    AUTHORITY_MODERATOR
            ).requestMatchers(
                "/discuss/delete",
                "/data/**"
            ).hasAnyAuthority(
                AUTHORITY_ADMIN
            ).anyRequest().permitAll()
        );

        http.exceptionHandling((exception) -> exception.authenticationEntryPoint(
                new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "You have to log in first"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                }
            ).accessDeniedHandler(new AccessDeniedHandler() {
                @Override
                public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(CommunityUtil.getJSONString(403, "You don't have authority"));
                    } else {
                        response.sendRedirect(request.getContextPath() + "/denied");
                    }
                }
            }
        ));

        http.logout((logout) -> logout.logoutUrl("/security/logout"));
        return http.build();
    }
}
