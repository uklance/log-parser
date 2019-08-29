package com.test;

import java.util.Optional;

public interface EventRepository {
	public Optional<Event> findOneById(String id);
	public void insert(Event event);
	public void update(Event event);
}
