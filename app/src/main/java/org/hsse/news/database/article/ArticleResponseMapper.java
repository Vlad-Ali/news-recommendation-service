package org.hsse.news.database.article;

import lombok.experimental.UtilityClass;
import org.hsse.news.api.schemas.response.article.ArticleListResponse;
import org.hsse.news.api.schemas.response.article.ArticleResponse;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.dto.ArticleDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public final class ArticleResponseMapper {

    public static ArticleListResponse getArticleListResponse(final List<ArticleDto> articleDtoList){
        final Map<String, ArticleResponse> articleResponseMap = new ConcurrentHashMap<>();
        for (final ArticleDto articleDto : articleDtoList){
            final List<String> topics = new ArrayList<>();
            for (final TopicDto topicDto : articleDto.topics()){
                topics.add(topicDto.description());
            }
            articleResponseMap.put(articleDto.url(), new ArticleResponse(articleDto.title(), articleDto.url(), articleDto.createdAt().toString(), new ArrayList<>(topics), articleDto.website().getUrl()));
        }
        return new ArticleListResponse(articleResponseMap.size(), articleResponseMap.values().stream().toList());
    }
}
