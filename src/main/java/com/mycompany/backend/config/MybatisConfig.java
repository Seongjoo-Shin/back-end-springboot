package com.mycompany.backend.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@MapperScan(basePackages={"com.mycompany.backend.dao"})
public class MybatisConfig { // springframework 프로젝트의 ch14_mybatis.xml을 자바로 구현
	
	@Resource
	private DataSource dataSource; // sqlSessionFactory의 매개변수에 작성해도 같은 효과
	
	@Resource
	WebApplicationContext wac;
	
	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
		ssfb.setDataSource(dataSource);
		ssfb.setConfigLocation(wac.getResource("classpath:mybatis/mapper-config.xml"));
		ssfb.setMapperLocations(wac.getResources("classpath:mybatis/mapper/*.xml"));
		
		return ssfb.getObject();
	}
	
	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory ssf) {
		SqlSessionTemplate sst = new SqlSessionTemplate(ssf);
		return sst; 
	}
}
