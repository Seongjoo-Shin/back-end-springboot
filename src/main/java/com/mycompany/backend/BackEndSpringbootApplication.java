package com.mycompany.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackEndSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackEndSpringbootApplication.class, args); // 독립으로 실행하기 위한 main / target의 .war파일이 있는 곳으로 가서 java -jar ~.war를 실행
	}

}
