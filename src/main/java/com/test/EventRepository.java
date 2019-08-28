package com.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;

public class EventRepository {
	private final JdbcTemplate jdbcTemplate;

	public EventRepository(JdbcTemplate jdbcTemplate) {
		super();
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<Event> findOneById(String id) {
		return jdbcTemplate.query("select * from event where id = ?", new Object[] { id }, this::extractEvent);
	}

	private static final String UPDATE_SQL = 
		"update event set start_time = ?, finish_time = ?, duration = ?, host = ?, type = ?, alert = ? where id = ?";

	private static final String INSERT_SQL = 
		"insert into event (id, start_time, finish_time, duration, host, type, alert) values (?,?,?,?,?,?,?)";
	
	public void insert(Event event) {
		jdbcTemplate.update(INSERT_SQL, new Object[] { event.getId(), event.getStartTime(), event.getFinishTime(), event.getDuration(),
				event.getHost(), event.getType(), event.isAlert() });
	}

	public void update(Event event) {
		jdbcTemplate.update(UPDATE_SQL, new Object[] { event.getStartTime(), event.getFinishTime(), event.getDuration(),
				event.getHost(), event.getType(), event.isAlert(), event.getId() });
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