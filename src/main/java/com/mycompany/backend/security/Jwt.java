package com.mycompany.backend.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Jwt {
  // 상수
  private static final String JWT_SECRET_KEY = "kosa12345"; // 비밀 키 생성, 암호화를 해줘야 함, 서버에 저장되어 있는 개인키
  private static final long ACCESS_TOKEN_DURATION = 1000*10; // access_token 유효기간, 30분
  public static final long REFRESH_TOKEN_DURATION = 1000*60*60*24; // refresh_token 유효기간, 24시간
  
  // AccessToken 생성
  public static String createAccessToken(String mid, String authority) {
    log.info("실행");
    String accessToken = null;
    
    try {
      accessToken = Jwts.builder()
          // 헤더 설정
          .setHeaderParam("alg", "HS256")
          .setHeaderParam("typ", "JWT")
          // 토큰의 유효기간 설정
          .setExpiration(new Date(new Date().getTime() + ACCESS_TOKEN_DURATION))
          // 페이로드 설정
          .claim("mid", mid)
          .claim("authority", authority)
          // 서명 설정
          .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
          // 토큰 생성
          .compact();
      
    } catch(Exception e) {
      log.info(e.getMessage());
    }
    return accessToken;
  }
  
  // RefreshToken 생성
  public static String createRefreshToken(String mid, String authority) {
    log.info("실행");
    String refreshToken = null;
    
    try {
      refreshToken = Jwts.builder()
          // 헤더 설정
          .setHeaderParam("alg", "HS256")
          .setHeaderParam("typ", "JWT")
          // 토큰의 유효기간 설정, 페이로드 설정
          .setExpiration(new Date(new Date().getTime() + REFRESH_TOKEN_DURATION))
          .claim("mid", mid)
          .claim("authority", authority)
          // 서명 설정
          .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
          // 토큰 생성
          .compact();
      
    } catch(Exception e) {
      log.info(e.getMessage());
    }
    
    return refreshToken;
  }
  
  // 유효성 검사
  public static boolean validateToken(String token) {
    log.info("실행");
    boolean result = false;
    
    try {
    result = Jwts.parser()
                 .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8")) // 비밀키가 맞는지 확인
                 .parseClaimsJws(token) // claim 객체를 리턴, 페이로드 값을 가져옴
                 .getBody() // 
                 .getExpiration()
                 .after(new Date());
    } catch(Exception e) {
      log.info(e.getMessage());
    }
    
    return result;
  }
  
  // 토큰 만료 시간 얻기
  public static Date getExpiration(String token) {
    log.info("실행");
    Date expiration = null;
    
    try {
      expiration = Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8")) // 비밀키가 맞는지 확인
          .parseClaimsJws(token) // claim 객체를 리턴, 페이로드 값을 가져옴
          .getBody() // 
          .getExpiration();

    } catch(Exception e) {
      log.info(e.getMessage());
    }
    return expiration;
  }
  
  // 인증 사용자 정보 얻기
  public static Map<String, String> getUserInfo(String token) {
    log.info("실행");
    Map<String, String> result = new HashMap<>();
    
    try {
      Claims claims = Jwts.parser()
                 .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8")) // 비밀키가 맞는지 확인
                 .parseClaimsJws(token) // claim 객체를 리턴, 페이로드 값을 가져옴
                 .getBody();
      result.put("mid",  claims.get("mid", String.class));
      result.put("authority",  claims.get("authority", String.class));
    } catch(Exception e) {
      log.info(e.getMessage());
    }
    
    return result;
  }
  
  // 요청 Authorizaion 헤더값에서 AccessToken 얻기
  // Bearer accesstoken.accesstoken.accesstoken
  public static String getAccessToken(String authorization) {
    String accessToken = null;
    if(authorization != null && authorization.startsWith("Bearer ")) {
      accessToken = authorization.substring(7);
    }
    
    return accessToken;
  }
  
//  public static void main(String[] args) {
//    String aToken = createAccessToken("user", "USER_ROLE");
//    System.out.println(validateToken(aToken));
//    
//    Date expiration = getExpiration(aToken);
//    System.out.println(expiration);
//    
//    Map<String, String> result = getUserInfo(aToken);
//    System.out.println(result);
//  }
  
  
}
