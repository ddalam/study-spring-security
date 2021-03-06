package com.study.security.config;

import com.study.security.student.StudentManager;
import com.study.security.teacher.TeacherManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final StudentManager studentManager;
    private final TeacherManager teacherManager;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /**
         * 정의한 AuthenticationProvider를 AuthenticationManager에 등록을 해줘야된다
         * authenticationProvider() 여기 들어가보면 provider들을 다 쌓아두게 되어있다
         *
         * 만약, teacher로 로그인 했을 경우
         * UsernamePasswordAuthenticationFilter의
         * return this.getAuthenticationManager().authenticate(authRequest); 이 부분에서
         * 인증을 처리해줄 provider들을 거치게 되는데
         * studentManager를 먼저 거치게 되지만 -> studentDB에서 발견을 못하고
         * teacherManager의 teacherDB에서 발견 -> teacher로 로그인이 되는 것
         */
        auth.authenticationProvider(studentManager);
        auth.authenticationProvider(teacherManager);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        CustomLoginFilter customLoginFilter = new CustomLoginFilter(authenticationManager());

        http
        .authorizeRequests(request->
                request.antMatchers("/", "/login").permitAll()
                .anyRequest().authenticated()
        )
            // customLoginFilter를 적용하기 위해 주석처리
//        .formLogin(
//                // login page를 통해서 usernamePasswordAuthenticationFilter가 동작하고
//                // usernamePasswordAuthenticationFilter는 usernamePasswordAuthenticationToken을 발행하기 때문에
//                // usernamePasswordAuthenticationToken을 대상으로 한 StudentManager가 발행을 해주게 된다
//                login -> login.loginPage("/login")
//                .permitAll()
//                .defaultSuccessUrl("/", false)
//                .failureUrl("/login-error")
//        )

        // UsernamePasswordAuthenticationFilter 자리를 CustomLoginFilter로 교체
        // 이렇게 하게 되면 defaultSuccessUrl, failureUrl 등을 따로 정의해주거나
        // 아니면 .formLogin()의 주석을 풀고 customLoginFilter와 같이 사용해도 된다
        .addFilterAt(customLoginFilter, UsernamePasswordAuthenticationFilter.class)
        .logout(logout -> logout.logoutSuccessUrl("/"))
        .exceptionHandling(e -> e.accessDeniedPage("/access-denied"));
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                ;
    }
}
