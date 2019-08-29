package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.sql.Driver;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public class LogParserIntegrationTest {
	private LogParser logParser;
	private EventRepository eventRepository;

	@Before
	public void before() {
		Driver driver = new org.hsqldb.jdbcDriver();
		DataSource dataSource = new SimpleDriverDataSource(driver, "jdbc:hsqldb:file:testdb");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		jdbcTemplate.execute("drop table event if exists");
		jdbcTemplate.execute(
				"create table event (" + 
				"id varchar(255) not null, " + 
				"start_time bigint null, " + 
				"finish_time bigint null, " + 
				"duration bigint null, " + 
				"host varchar(255) null, " + 
				"type varchar(255) null, " + 
				"alert varchar(255) null, " +
				"PRIMARY KEY (id)" +
				")");
		
		int threadCount = 10;
		eventRepository = new EventRepositoryImpl(jdbcTemplate);
		logParser = new LogParser(eventRepository, threadCount);
	}
	
	@Test
	public void testEnqueue() throws Exception {
		logParser.enqueue(getResource("sample-log-small.json"));
		logParser.shutdownAndAwaitTermination(1, TimeUnit.SECONDS);
		Event scsmbstgra = eventRepository.findOneById("scsmbstgra").get();
		Event scsmbstgrb = eventRepository.findOneById("scsmbstgrb").get();
		Event scsmbstgrc = eventRepository.findOneById("scsmbstgrc").get();
		assertThat(scsmbstgra.getHost()).isEqualTo("12345");
		assertThat(scsmbstgra.getType()).isEqualTo("APPLICATION_LOG");
		assertThat(scsmbstgrc.getDuration()).isEqualTo(8L);
		assertThat(scsmbstgrc.isAlert()).isTrue();
		assertThat(scsmbstgrb.getDuration()).isEqualTo(3L);
		assertThat(scsmbstgrb.isAlert()).isFalse();
	}
	
	protected InputStream getResource(String path) {
		InputStream in = getClass().getClassLoader().getResourceAsStream(path);
		if (in == null) throw new RuntimeException("No such resource " + path);
		return in;
	}
}
