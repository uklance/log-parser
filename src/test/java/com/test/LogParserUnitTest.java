package com.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogParserUnitTest {
	@Mock
	private EventRepository eventRepository;
	
	private LogParser logParser;
	
	private Map<String, Event> eventMap;

	@Before
	public void before() {
		eventMap = new ConcurrentHashMap<String, Event>();
		int threadCount = 10;
		logParser = new LogParser(eventRepository, threadCount);
		
		when(eventRepository.findOneById(any())).thenAnswer(this::findOneById);
		doAnswer(this::save).when(eventRepository).insert(any());
		doAnswer(this::save).when(eventRepository).update(any());
	}
	
	@Test
	public void testEnqueue() throws Exception {
		logParser.enqueue(getResource("sample-log-small.json"));
		logParser.awaitTermination(10, TimeUnit.MILLISECONDS);
		
		assertThat(eventMap).containsOnlyKeys("scsmbstgra", "scsmbstgrb", "scsmbstgrc");
		Event scsmbstgrc = eventMap.get("scsmbstgrc");
		Event scsmbstgrb = eventMap.get("scsmbstgrb");
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
	
	protected Optional<Event> findOneById(InvocationOnMock invocation) {
		String id = invocation.getArgumentAt(0, String.class);
		return eventMap.containsKey(id) ? Optional.of(eventMap.get(id)) : Optional.empty();
	}
	protected Void save(InvocationOnMock invocation) {
		Event event = invocation.getArgumentAt(0, Event.class);
		eventMap.put(event.getId(), event);
		return null;
	}
}
