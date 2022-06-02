package com.mycompany.backend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.backend.dto.Board;
import com.mycompany.backend.dto.Pager;
import com.mycompany.backend.service.BoardService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/board")
public class BoardController {
	
	@Resource
	private BoardService boardService;
	
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue="1") int pageNo) {
		log.info("실행");
		int totalRows = boardService.getTotalBoardNum();
		
		Pager pager = new Pager(5, 5, totalRows, pageNo);
		
		List<Board> list = boardService.getBoards(pager);
		Map<String, Object> map  = new HashMap<>();
		
		map.put("boards", list);
		map.put("pager", pager);
		
		return map;
	}
	
	@PostMapping("/")
	public Board create(Board board) {
		log.info("실행");
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				File file = new File("C:/Temp/uploadfiles/" + board.getBattachsname());
				mf.transferTo(file);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		boardService.writeBoard(board);
		Board dbBoard = boardService.getBoard(board.getBno(), false);
		return dbBoard;
	}
	
	@PutMapping("/")
	public Board update(Board board) {
		log.info("실행");
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				File file = new File("C:/Temp/uploadfiles/" + board.getBattachsname());
				mf.transferTo(file);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		boardService.updateBoard(board);
		Board dbBoard = boardService.getBoard(board.getBno(), false);
		return dbBoard;
	}
	
	// http://localhost/board/3 -> bno가 3인 게시물
	@GetMapping("/{bno}")
	public Board read(@PathVariable int bno, @RequestParam(defaultValue="false") boolean hit) {
		log.info("실행");
		Board dbBoard = boardService.getBoard(bno, hit);
		return dbBoard;
	}
	
	@DeleteMapping("/{bno}")
	public Map<String, String> delete(@PathVariable int bno) {
		
		boardService.removeBoard(bno);
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		
		return map;
	}
	
	@GetMapping("/battach/{bno}")
	public ResponseEntity<InputStreamResource> download(@PathVariable int bno) throws Exception {
		Board board = boardService.getBoard(bno, false);
		String battachoname = board.getBattachoname();
		
		if(battachoname == null) return null;
		
		// 파일 일므이 한글일 경우에 설정
		battachoname = new String(battachoname.getBytes("UTF-8"),"ISO-8859-1");
		
		// 파일 입력 스트림 생성
		FileInputStream fis = new FileInputStream("C:/Temp/uploadfiles/" + board.getBattachsname());
		InputStreamResource resource = new InputStreamResource(fis);
		
		// 응답 생성
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + battachoname + "\";")
				.header(HttpHeaders.CONTENT_TYPE, board.getBattachtype())
				.body(resource);
	}
}












