package com.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.LogEntry.State;

public class LogParser {
	private static final Logger logger = LoggerFactory.getLogger(LogParser.class);
	private static final long DURATION_ALERT_THRESHOLD_MILLIS = 4;
	private final EventRepository eventRepository;
	private final ExecutorService[] executors;
	private final int threadCount;
	
	public LogParser(EventRepository eventRepository, int threadCount) {
		super();
		this.eventRepository = eventRepository;
		this.executors = new ExecutorService[threadCount];
		this.threadCount = threadCount;
		for (int i = 0; i < threadCount; ++i) {
			this.executors[i] = Executors.newSingleThreadExecutor();
		}
	}

	public void enqueue(InputStream logStream) throws IOException {
		JsonFactory jfactory = new JsonFactory();
		ObjectMapper objectMapper = new ObjectMapper();
		jfactory.setCodec(objectMapper);
		try (JsonParser jParser = jfactory.createParser(logStream)) {
			while (jParser.nextToken() != null) {
				LogEntry logEntry = jParser.readValueAs(LogEntry.class);
				int executorIndex = Math.abs(logEntry.getId().hashCode() % threadCount);
				executors[executorIndex].submit(() -> processLogEntry(logEntry));
			}
		}
	}
	
	public void shutdownAndAwaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
		for (ExecutorService executor : executors) {
			executor.shutdown();
		}
		for (ExecutorService executor : executors) {
			executor.awaitTermination(timeout, timeUnit);
		}
	}
	
	private void processLogEntry(LogEntry logEntry) {
		try {
			Optional<Event> optional = eventRepository.findOneById(logEntry.getId());
			Event event;
			if (optional.isPresent()) {
				event = optional.get();
			} else {
				event = new Event();
				event.setId(logEntry.getId());
			}
			if (logEntry.getState() == State.STARTED) {
				event.setStartTime(logEntry.getTimestamp());
			} else if (logEntry.getState() == State.FINISHED) {
				event.setFinishTime(logEntry.getTimestamp());
			}
			if (logEntry.getHost() != null) {
				event.setHost(logEntry.getHost());
			}
			if (logEntry.getType() != null) {
				event.setType(logEntry.getType());
			}
			if (event.getStartTime() != null && event.getFinishTime() != null) {
				event.setDuration(event.getFinishTime() - event.getStartTime());
				if (event.getDuration() > DURATION_ALERT_THRESHOLD_MILLIS) {
					event.setAlert(true);
				}
			}
			if (optional.isPresent()) {
				logger.debug("update " + event);
				eventRepository.update(event);
			} else {
				logger.debug("insert " + event);
				eventRepository.insert(event);
			}
		} catch (Throwable t) {
			logger.error("Error processing " + logEntry, t);
		}
	}
}
