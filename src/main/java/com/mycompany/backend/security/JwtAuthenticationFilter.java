package com.mycompany.backend.security;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  
  private RedisTemplate redisTemplate;
  
  public void setRedisTemplate(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
     log.info("실행");
     
     // 요청 헤더로부터 Authorizaion 헤더 값 얻기
     String authorizaion = request.getHeader("Authorization");
     
     // AccessToken 추출
     String accessToken = Jwt.getAccessToken(authorizaion);
     
     // 검증 작업
     if(accessToken != null && Jwt.validateToken(accessToken)) {
       // Redis에 AccessToken의 존재여부를 확인, accesstoken이 redis의 키로 존재하지 않으면 검증되면 안댐 (로그아웃이 되었을 때 accesstoken이 사라지니까)
       ValueOperations<String, String> vo = redisTemplate.opsForValue();
       String redisRefreshToken = vo.get(accessToken);
       
       if(redisRefreshToken != null) {
         // 인증 처리
         Map<String, String> userInfo = Jwt.getUserInfo(accessToken);
         String mid = userInfo.get("mid");
         String authority = userInfo.get("authority");
         // 직접 패스워드가 필요없음
         UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mid, null, AuthorityUtils.createAuthorityList(authority));
         SecurityContext securityContext = SecurityContextHolder.getContext();
         securityContext.setAuthentication(authentication);  
       }
     }
     
     filterChain.doFilter(request, response);
  }

}
