package org.hsse.news.bot;

import lombok.*;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.dto.ArticleDto;
import org.hsse.news.dto.ResponseUserArticleDto;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserState {
  private UserDto user;
  private ArticleDto article;
  private Integer currentIndex;
  private List<ResponseUserArticleDto> knownArticles;
  private  List<ArticleDto> unknownArticles;
}
