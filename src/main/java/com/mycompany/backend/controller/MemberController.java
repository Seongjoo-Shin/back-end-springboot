package com.mycompany.backend.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.backend.dto.Member;
import com.mycompany.backend.security.Jwt;
import com.mycompany.backend.service.MemberService;
import com.mycompany.backend.service.MemberService.JoinResult;
import com.mycompany.backend.service.MemberService.LoginResult;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/member")
public class MemberController {
  
  @Resource
  private MemberService memberService;  
  
  @Resource
  private PasswordEncoder passwordEncoder;
  
  @Resource
  private RedisTemplate<String, String> redisTemplate;
  
  @PostMapping("/join")
  public Map<String, Object> join(@RequestBody Member member) { // @RequestBody를 사용하려면 Member가 json 형태로 와야함
    log.info("실행");
    
    // 계정 활성화
    member.setMenabled(true);
    
    // 패스워드 암호화
    member.setMpassword(passwordEncoder.encode(member.getMpassword()));
    
    // 회원가입 처리
    JoinResult joinResult = memberService.join(member);
    
    // 응답 내용 설정
    Map<String, Object> map = new HashMap<>();
    
    if(joinResult == JoinResult.SUCCESS) {
      map.put("result", "success");  
    } else if(joinResult == JoinResult.DUPLICATED) {
      map.put("result", "duplicated");
    } else {
      map.put("result", "fail");
    }
    return map;
  }
  
  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody Member member) {
    
    log.info("실행");
    
    // mid와 mpassword가 없는 경우
    if(member.getMid() == null || member.getMpassword() == null) {
      return ResponseEntity.status(401).body("mid or mpassword cannot be null");
    }
    
    // 로그인 결과 얻기
    LoginResult loginResult = memberService.login(member);
    
    if(loginResult != LoginResult.SUCCESS) {
      return ResponseEntity.status(401).body("mid or mpassword is wrong");
    }

    Member dbMember = memberService.getMember(member.getMid());
    String accessToken = Jwt.createAccessToken(member.getMid(), dbMember.getMrole());
    String refreshToken = Jwt.createRefreshToken(member.getMid(), dbMember.getMrole());
    
    // Redis에 저장
    ValueOperations<String, String> vo = redisTemplate.opsForValue();
    vo.set(accessToken, refreshToken, Jwt.REFRESH_TOKEN_DURATION, TimeUnit.MILLISECONDS); // 키 : accessToken , 값 : refreshToken, 만료기간 : refreshToken의 만료기간과 동일
    
    // 쿠키 생성, 브라우저 js에서 읽을 수 없음
    String refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                                              .httpOnly(true)
                                              .secure(false)
                                              .path("/")
                                              .maxAge(Jwt.REFRESH_TOKEN_DURATION/1000)
                                              .domain("localhost")
                                              .build()
                                              .toString(); 
    // 본문 생성
    String json = new JSONObject()
                      .put("accessToken", accessToken)
                      .put("mid", member.getMid())
                      .toString();
    // 응답 설정
    return ResponseEntity
                // 응답 상태 코드 : 200
                .ok()
                // 응답 헤더 추가
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                // 응답 바디 추가
                .body(json);
  }
  
  // 실패한 accesstoken을 같이 보내줘야함
  // 마지막에 만료된 accesstoken인지 알아야 함, accesstoken의 유효성 검사를 할 수 없으니까
  @GetMapping("/refreshToken")
  public ResponseEntity<String> refreshToken(@RequestHeader("Authorization") String authorization, @CookieValue("refreshToken") String refreshToken ) {
    
    // AccessToken 얻기
    String accessToken = Jwt.getAccessToken(authorization);
    if(accessToken == null) {
      return ResponseEntity.status(401).body("no access token");
    }
    
    // RefreshToken 여부 
    if(refreshToken == null) {
      return ResponseEntity.status(401).body("no refresh token");
    }
    
    // 동일한 토큰인지 확인
    ValueOperations<String, String> vo = redisTemplate.opsForValue();
    String redisRefreshToken = vo.get(accessToken);
    if(redisRefreshToken == null) {
      return ResponseEntity.status(401).body("invalid access token");
    }
    if(!refreshToken.equals(redisRefreshToken)) {
      return ResponseEntity.status(401).body("invalid refresh token");
    }
    
    // 새로운 AccessToken 생성
    Map<String, String> userInfo = Jwt.getUserInfo(refreshToken);
    String mid = userInfo.get("mid");
    String authority = userInfo.get("authority");
    String newAccessToken = Jwt.createAccessToken(mid, authority);
    
    // Redis에 저장되어 있는 기존 정보 삭제
    redisTemplate.delete(accessToken);
    
    // Redis에 새로운 정보를 저장, refreshtoken의 남은 시간만 새롭게 저장되도록 해야함, refresh토큰이 만료 되기 전
    Date expiration = Jwt.getExpiration(refreshToken);
    vo.set(newAccessToken, refreshToken, expiration.getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
    
    // 응답 설정
    String json = new JSONObject()
                      .put("accessToken", newAccessToken)
                      .put("mid", mid)
                      .toString();
    
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/json").body(json);
  }
  
  // 로그아웃시에도 accessToken을 정확하게 사용해야함
  @GetMapping("/logout")
  public ResponseEntity<String> logout(@RequestHeader("authorization") String authorization) {
    log.info("실행");
    // AccessToken 얻기
    String accessToken = Jwt.getAccessToken(authorization);
    if(accessToken == null) {
      return ResponseEntity.status(401).body("invalid access token");
    }
    
    // Redis에 저장된 인증 정보를 삭제
    redisTemplate.delete(accessToken);
    
    // RefreshToken 쿠키 삭제
    String refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                                              .httpOnly(true)
                                              .secure(false)
                                              .path("/")
                                              .maxAge(0) // 이부분을 0으로 주게 되면 쿠키 삭제의 효과
                                              .domain("localhost")
                                              .build()
                                              .toString(); 
    
    return ResponseEntity
              .ok()
              .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)
              .body("success");
  }
}
