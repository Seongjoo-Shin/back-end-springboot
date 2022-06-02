package com.mycompany.backend.controller;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.backend.dto.Board;
import com.mycompany.backend.dto.Member;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController // 모든 요청 매핑 메소드에 @ResponseBody가 자동으로 붙음 ( JSON 형식을 사용하기 위해 )
@RequestMapping("/rest")
public class RestControllerTest {

	@GetMapping("/getObject")
	public Board getObject() {

		log.info("실행");
		Board board = new Board();
		board.setBno(1);
		board.setBtitle("제목");
		board.setBcontent("내용");
		board.setMid("user");
		board.setBdate(new Date());
		return board;
	}

	@GetMapping("/getMap")
	public Map<String, Object> getMap() {
		log.info("실행");

		Map<String, Object> map = new HashMap<>();
		map.put("name", "홍길동");
		map.put("age", 25);

		Board board = new Board();
		board.setBno(1);
		board.setBtitle("제목");
		board.setBcontent("내용");
		board.setMid("user");
		board.setBdate(new Date());
		map.put("board", board);

		return map;
	}

	@GetMapping("/getArray")
	public String[] getArray() {
		log.info("실행");
		String[] array = { "Java", "Spring", "Vue" };
		return array;
	}

	@GetMapping("/getList1")
	public List<String> getList1() {
		log.info("실행");
		List<String> list = new ArrayList<>();
		list.add("Java");
		list.add("Spring Boot");
		list.add("Vue.js");
		return list;
	}

	@RequestMapping("getList2")
	public List<Board> getList2() {
		log.info("실행");
		List<Board> list = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			Board board = new Board();
			board.setBno(i);
			board.setBtitle("제목" + i);
			board.setBcontent("내용" + i);
			board.setMid("user");
			board.setBdate(new Date());
			list.add(board);
		}
		return list;
	}
	
	@GetMapping("/useHttpServletResponse")
	public void getHeader(HttpServletResponse response) throws Exception {
		
		// 응답 헤더 설정
		response.setContentType("application/json; charset=UTF-8");
		response.addHeader("TestHeader", "value");
		
		Cookie cookie = new Cookie("refreshToken", "xxxxx");
		response.addCookie(cookie);
		
		// 응답 본문 설정
		PrintWriter pw = response.getWriter();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", "success");
		String json = jsonObject.toString();
		
		pw.println(json);
		
		pw.flush();
		pw.close();
	}
	
	@GetMapping("/useResponseEntity")
	public ResponseEntity<String> useResponseEntity(){
	// 제네릭의 타입은 응답본문에 어떠한 타입을 넣을 것인가를 결정 JSON일때는 Map<String, Object>
		log.info("실행");
		/*BodyBuilder bodyBuilder = ResponseEntity.ok();
		ResponseEntity<String> result = bodyBuilder.body("success");
		return result;*/
		
		/*StringBuilder sb = new StringBuilder();
		sb.append("a");
		sb.append("b");
		sb.append("c");
		String result = sb.toString();
		
		String result2 = new StringBuilder()
							.append("a")
							.append("b")
							.append("c")
							.toString();*/
		
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", "success");
		String json = jsonObject.toString();
		
		String cookieStr = ResponseCookie.from("refreshToken", "xxx")
							.build()
							.toString();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, "application/json") // 얘가 없으면 text/plain 타입, 브라우저가 기본으로 utf-8로 인식
				.header("TextHeader", "value")
				.header(HttpHeaders.COOKIE, cookieStr)
				.body(json);
	}
	
	@RequestMapping("/sendQueryString")
	public Member sendQueryString(Member member) {
		log.info("실행");
		return member;
	}
	
	@PostMapping("/sendJson")
	public Member sendJson(@RequestBody Member member) {
		log.info("실행");
		return member;
	}
	
	// 첨부파일 관련 rest api
	@PostMapping("/sendMultipartFormData")
	public Map<String, String> sendMultipartFormData(String title, MultipartFile attach) throws Exception {
		String savedFile = new Date().getTime() + "-" + attach.getOriginalFilename();
		attach.transferTo(new File("C:/Temp/uploadfiles" + savedFile));
		
		Map<String, String> map = new HashMap<>();
		
		map.put("result", "success");
		map.put("savedFile", savedFile);
		
		return map;
	}
}



























