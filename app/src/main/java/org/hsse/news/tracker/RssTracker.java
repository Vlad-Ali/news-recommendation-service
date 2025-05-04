package org.hsse.news.tracker;

import ai.onnxruntime.OrtException;
import com.rometools.rome.io.FeedException;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.application.OnnxApplication;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.database.website.models.WebsiteId;
import org.hsse.news.dto.RequestArticleDto;
import org.hsse.news.dto.ResponseArticleDto;
import org.hsse.news.parser.ParsedArticle;
import org.hsse.news.parser.RssParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@EnableScheduling
public class RssTracker {
  private final OnnxApplication onnxApplication;
  private final TopicService topicService;
  private final ArticlesService articlesService;
  private final WebsiteService websiteService;
  private static final Float MINIMUM_PERCENT = 0.1F;
  private static final Logger LOG = LoggerFactory.getLogger(RssTracker.class);

  public RssTracker(final OnnxApplication onnxApplication, final TopicService topicService, final ArticlesService articlesService, final WebsiteService websiteService) {
    this.onnxApplication = onnxApplication;
    this.topicService = topicService;
    this.articlesService = articlesService;
    this.websiteService = websiteService;
  }

  private List<WebsiteInfo> getAllWebsites() {
    return websiteService.getAllWebsites();
  }

  private List<TopicInfo> getAllTopics() {
    return topicService.getAllTopics();
  }


  private List<ParsedArticle> getWebsiteArticles(final String url) throws IOException, FeedException {
    return RssParser.parse(new URL(url), url);
  }

  private void matchTopics(final ParsedArticle parsedArticle, final List<TopicInfo> topicList, final WebsiteId websiteId) throws OrtException {
    final List<String> nameOfTopics = new ArrayList<>();
    for (final TopicInfo topic : topicList) {
      nameOfTopics.add(topic.description());
    }
    final Map<String, Float> mapOfTopics = onnxApplication.predict(parsedArticle.description(), nameOfTopics);
    for (final TopicInfo topic : topicList) {
      LOG.debug(topic.description() + ": " + mapOfTopics.get(topic.description()));
      if (mapOfTopics.get(topic.description()).compareTo(MINIMUM_PERCENT) > 0) {
        if (articlesService.isArticleWithUrlAdd(parsedArticle.link())) {
          articlesService.addTopic(articlesService.findArticleIdByUrl(parsedArticle.link()).get(), new TopicId(topic.topicID()));
        } else {
          final ResponseArticleDto responseArticleDto = articlesService.create(new RequestArticleDto(parsedArticle.name(), parsedArticle.link(), Timestamp.from(parsedArticle.date()), websiteId.value()));
          articlesService.addTopic(new ArticleId(responseArticleDto.articleId()), new TopicId(topic.topicID()));
        }
      }
    }
    LOG.debug("{} is add with topics", parsedArticle.name());
  }

  @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 10_000 * 1000)
  @Transactional
  public void addNewArticles() {
    final List<WebsiteInfo> websites = websiteService.getAllWebsites();
    final List<TopicInfo> topics = topicService.getAllTopics();
    for (final WebsiteInfo website : websites) {
      LOG.debug("{} is being parsed", website.url());
      try {
        final List<ParsedArticle> parsedArticles = getWebsiteArticles(website.url());
        for (final ParsedArticle parsedArticle : parsedArticles) {
          if (articlesService.isArticleWithUrlAdd(parsedArticle.link())) {
            continue;
          }
          try {
            matchTopics(parsedArticle, topics, new WebsiteId(website.websiteId()));
          } catch (Exception e) {
            LOG.debug("Got exception on matching topics: {}", e.getMessage());
          }
        }
      } catch (Exception e) {
        LOG.debug("Got exception on connection to website: {}", e.getMessage());
      }

    }
  }
}
