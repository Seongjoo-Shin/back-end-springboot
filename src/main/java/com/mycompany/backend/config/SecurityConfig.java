package com.mycompany.backend.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mycompany.backend.security.JwtAuthenticationFilter;

import lombok.extern.log4j.Log4j2;


@Log4j2
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
  @Resource
  private RedisTemplate redisTemplate;
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    log.info("실행");
    // 서버 세션 비활성화, Http Session 객체의 생성을 막음, jwt는 jsessionid를 쓰지 않기때문에
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // JSESSIONID 자체가 생성되지 않음
    
    // 폼 로그인 비활성화, spa 방식으로 하기위해서 form 비활성화 -> form은 mpa 방식
    http.formLogin().disable(); // form 자체가 나오지 않게 막음
    
    // 사이트간 요청 위조 방지 비활성화
    http.csrf().disable(); // rest api에서는 form을 사용하지 않음
    
    // 요청 경로 권한 설정 / /board관련 요청이 오면 인증된 것만 허용하겠다, 나머지인 경우는 모두 허용
    http.authorizeRequests()
        .antMatchers("/board/**").authenticated() // board는 인증 필요 ( 로그인 필요 )
        .antMatchers("/**").permitAll(); // board를 제외한 나머지는 인증 불필요, spring security가 동작ㄴ

    // CORS 설정 (다른 도메인의 JavaScript로 접근을 할 수 있도록 허용)
    http.cors();
    
    // JWT 인증 필터 추가
    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
  }
  
  // 여기 SecurityConfig 클래스에서만 사용되는 메소드이므로 관리객체로 만들어줄 필요가 없음
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter();
    jwtAuthenticationFilter.setRedisTemplate(redisTemplate);
    return jwtAuthenticationFilter;
  }
  
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    log.info("실행");
    // ↓ 방법은 폼 인증 방식에서 사용하는 방식 ( JWT 인증 방식에서는 사용하지 않음 )
    /*DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(new CustomUserDetailsService()); // db에서 뭘 갖고 올것인지
    provider.setPasswordEncoder(passwordEncoder()); // 패스워드 인코더는 뭘 쓸것인지
    auth.authenticationProvider(provider);*/
    
  }
  
  @Override
  public void configure(WebSecurity web) throws Exception {
    log.info("실행");
    DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
    defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchyImpl());   
    web.expressionHandler(defaultWebSecurityExpressionHandler);
    
    /*web.ignoring() // spring security가 동작을 아예 안함
    // ↓ 얘네는 SPA 방식에서는 사용하지 않는 방법, MPA 방식에서만 사용하는 방법
    // MPA에서 시큐리티를 적용하지 않는 경로 설정
       .antMatchers("/images/**")
       .antMatchers("/css/**")
       .antMatchers("/js/**")
       .antMatchers("/bootstrap/**")
       .antMatchers("/jquery/**")
       .antMatchers("/favicon.ico");*/
  }
  
  @Bean
  public PasswordEncoder passwordEncoder() { // 회원가입할 때도 사용해야 하므로 관리객체로 등록 ( @Bean )
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    return new BCryptPasswordEncoder(); // bcrypt 알고리즘일 때만 해당
  }
  
  @Bean
  public RoleHierarchyImpl roleHierarchyImpl() {
     log.info("실행");
     RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
     roleHierarchyImpl.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
     return roleHierarchyImpl;
  }
  
  // REST API에서만 사용하는 메소드
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    log.info("실행");
      CorsConfiguration configuration = new CorsConfiguration();
      // ↓ 부분의 값이 * 로 설정되어 있다면 공공 api가 됨
      // 모든 요청 사이트 허용, 도메인 허용
      configuration.addAllowedOrigin("*");
      // 모든 요청 방식 허용, get/post/put/delete/update 어떤 방식을 허용할 것인지
      configuration.addAllowedMethod("*"); 
      // 모든 요청 헤더 허용
      configuration.addAllowedHeader("*");
      // 모든 URL 요청에 대해서 위 내용을 적용
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }
}
