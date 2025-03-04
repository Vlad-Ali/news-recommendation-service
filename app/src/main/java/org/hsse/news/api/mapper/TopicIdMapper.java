package org.hsse.news.api.mapper;

import org.hsse.news.database.topic.models.TopicId;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;


public class TopicIdMapper implements RowMapper<TopicId> {

  @Override
  public TopicId map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new TopicId(rs.getLong("topic_id"));
  }
}
