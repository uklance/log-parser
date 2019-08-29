package com.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;

public class EventRepositoryImpl implements EventRepository {
	private final JdbcTemplate jdbcTemplate;

	public EventRepositoryImpl(JdbcTemplate jdbcTemplate) {
		super();
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Optional<Event> findOneById(String id) {
		return jdbcTemplate.query("select * from event where id = ?", new Object[] { id }, this::extractEvent);
	}

	@Override
	public void insert(Event event) {
		jdbcTemplate.update(
			"insert into event (id, start_time, finish_time, duration, host, type, alert) values (?,?,?,?,?,?,?)", 
			new Object[] { event.getId(), event.getStartTime(), event.getFinishTime(), event.getDuration(), event.getHost(), event.getType(), event.isAlert() }
		);
	}

	@Override
	public void update(Event event) {
		jdbcTemplate.update(
			"update event set start_time = ?, finish_time = ?, duration = ?, host = ?, type = ?, alert = ? where id = ?", 
			new Object[] { event.getStartTime(), event.getFinishTime(), event.getDuration(), event.getHost(), event.getType(), event.isAlert(), event.getId() }
		);
	}

	private Optional<Event> extractEvent(ResultSet rs) throws SQLException {
		if (!rs.next()) {
			return Optional.empty();
		} else {
			Event event = new Event();
			event.setId(rs.getString("id"));
			event.setAlert("true".equals(rs.getString("alert")));
			event.setStartTime(getLong(rs, "start_time"));
			event.setFinishTime(getLong(rs, "finish_time"));
			event.setDuration(rs.getLong("duration"));
			event.setHost(rs.getString("host"));
			event.setType(rs.getString("type"));
			return Optional.of(event);
		}
	};

	private Long getLong(ResultSet rs, String name) throws SQLException {
		long value = rs.getLong(name);
		return rs.wasNull() ? null : value;
	}
}
