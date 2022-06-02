package com.mycompany.backend.service;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mycompany.backend.dao.MemberDao;
import com.mycompany.backend.dto.Member;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MemberService {
	//열거 타입 선언
	public enum JoinResult {
		SUCCESS,
		FAIL,
		DUPLICATED
	}
	public enum LoginResult {
		SUCCESS,
		FAIL_MID,
		FAIL_MPASSWORD,
		FAIL
	}
	
	@Resource
	private MemberDao memberDao;
	
	@Resource
	private PasswordEncoder passwordEncoder;
	
	//회원 가입을 처리하는 비즈니스 메소드(로직)
	public JoinResult join(Member member) {
		try {
			//이미 가입된 아이디인지 확인
			Member dbMember = memberDao.selectByMid(member.getMid()); 
			
			//DB에 회원 정보를 저장
			if(dbMember == null) {
				memberDao.insert(member);
				return JoinResult.SUCCESS;
			} else {
				return JoinResult.DUPLICATED;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return JoinResult.FAIL;
		}
	}

	public LoginResult login(Member member) {
		try {
			//이미 가입된 아이디인지 확인
			Member dbMember = memberDao.selectByMid(member.getMid()); 
			//확인 작업
			if(dbMember == null) {
				return LoginResult.FAIL_MID;
			} else if(!passwordEncoder.matches(member.getMpassword(), dbMember.getMpassword())) {
				return LoginResult.FAIL_MPASSWORD;
			} else {
				return LoginResult.SUCCESS;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return LoginResult.FAIL;
		}
	}
	
	public Member getMember(String mid) {
		return memberDao.selectByMid(mid);
	}
}








