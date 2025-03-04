package org.hsse.news.api.mapper;

import org.hsse.news.database.website.models.WebsiteId;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WebsiteIdMapper implements RowMapper<WebsiteId> {
  @Override
  public WebsiteId map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new  WebsiteId(rs.getLong("website_id"));
  }
}
