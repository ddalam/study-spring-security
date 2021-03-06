package com.study.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Order(1)   // security filter chain을 여러개 구성할 경우 순서 지정
@EnableWebSecurity(debug = true)    // security 필터를 어떻게 구성했는지 터미널 로그로 확인할 수 있음
@EnableGlobalMethodSecurity(prePostEnabled = true)  // pre post로 권한 체크를 하겠다 -> 권한 체크 모듈이 작동됨
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // user 만들기 - 추가하면 yml에 등록한 유저는 먹히지 않음
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(User.builder()
                            .username("user2")
                            .password(passwordEncoder().encode("2222"))   // 패스워드를 인코딩하지 않으면 에러가 난다
                            .roles("USER"))
                .withUser(User.builder()
                            .username("admin")
                            .password(passwordEncoder().encode("3333"))
                            .roles("ADMIN"));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Spring Security가 기본으로 사용하는 인코더
    }

    // 스프링 시큐리티는 기본적으로 모든 페이지를 다 막아둔다
    // security 필터를 구성하는 설정
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.antMatcher("/api/**"); // 어떤 request에 대해서 필터 체인이 동작할 것인지 설정
        http.authorizeRequests((requests) ->
//                requests.anyRequest().authenticated() // 모든 페이지를 다 인증해라
                requests.antMatchers("/").permitAll()   // "/" 주소는 모든 사용자에게 접근을 허락
                        .anyRequest().authenticated()   // 나머지는 인증해야 접근을 허락
        );
        http.formLogin();
        http.httpBasic();
    }
}
