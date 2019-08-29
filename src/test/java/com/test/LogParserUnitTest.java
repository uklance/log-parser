package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class LogParserUnitTest {
	private MockEventRepository eventRepository;;
	private LogParser logParser;

	@Before
	public void before() {
		int threadCount = 10;
		eventRepository = new MockEventRepository(); 
		logParser = new LogParser(eventRepository, threadCount);
	}
	
	@Test
	public void testEnqueue() throws Exception {
		logParser.enqueue(getResource("sample-log-small.json"));
		logParser.shutdownAndAwaitTermination(1, TimeUnit.SECONDS);
		
		assertThat(eventRepository.getIds()).containsOnly("scsmbstgra", "scsmbstgrb", "scsmbstgrc");
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
	
	protected static class MockEventRepository implements EventRepository {
		private final Map<String, Event> eventMap = new ConcurrentHashMap<String, Event>();

		@Override
		public Optional<Event> findOneById(String id) {
			Event event = eventMap.get(id);
			return event == null ? Optional.empty() : Optional.of(event);
		}

		@Override
		public void insert(Event event) {
			cloneAndPut(event);
		}

		@Override
		public void update(Event event) {
			cloneAndPut(event);
		}
		
		public Set<String> getIds() {
			return eventMap.keySet();
		}
		
		protected void cloneAndPut(Event event) {
			eventMap.put(event.getId(), (Event) event.clone());
		}
	}
}
