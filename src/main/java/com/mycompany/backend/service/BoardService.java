package com.mycompany.backend.service;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mycompany.backend.dao.BoardDao;
import com.mycompany.backend.dto.Board;
import com.mycompany.backend.dto.Pager;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class BoardService {
	
	@Resource
	private BoardDao boardDao;
	
	public List<Board> getBoards(Pager pager) {
		log.info("실행");
		return boardDao.selectByPage(pager);
	}
	
	public Board getBoard(int bno, boolean hit) {
		log.info("실행");
		if(hit) {
			boardDao.updateBhitcount(bno);
		}
		return boardDao.selectByBno(bno);
	}
	
	public int getTotalBoardNum() {
		log.info("실행");
		return boardDao.count();
	}
	
	public void writeBoard(Board board) {
		log.info("실행");
		boardDao.insert(board);
	}
	
	public void updateBoard(Board board) {
		log.info("실행");
		boardDao.update(board);
	}
	
	public void removeBoard(int bno) {
		log.info("실행");
		boardDao.deleteByBno(bno);
	}
}








