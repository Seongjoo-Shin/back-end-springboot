package com.mycompany.backend.dto;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class Board {
	private int bno;
	private String btitle;
	private String bcontent;
	private String mid;
	private Date bdate;
	private int bhitcount;
	private MultipartFile battach;
	private String battachoname;
	private String battachsname;
	private String battachtype;	
}
