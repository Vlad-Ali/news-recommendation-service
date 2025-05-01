package org.hsse.news.database.article;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hsse.news.database.article.repositories.JpaArticleRepository;
import org.hsse.news.database.entity.*;
import org.hsse.news.database.article.exceptions.ArticleNotFoundException;
import org.hsse.news.database.topic.exceptions.TopicNotFoundException;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.topic.repositories.JpaTopicsRepository;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.JpaUsersRepository;
import org.hsse.news.database.website.repositories.JpaWebsitesRepository;
import org.hsse.news.dto.ArticleDto;
import org.hsse.news.dto.RequestArticleDto;
import org.hsse.news.dto.ResponseArticleDto;
import org.hsse.news.dto.ResponseUserArticleDto;
import org.hsse.news.dto.ResponseWebsiteDto;
import org.hsse.news.util.Grade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticlesService {

    private final JpaArticleRepository articleRepository;
    private final JpaTopicsRepository topicRepository;
    private final JpaWebsitesRepository websiteRepository;
    private final JpaUsersRepository userRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ArticlesService.class);

    public ArticlesService(final JpaArticleRepository articleRepository,final JpaTopicsRepository topicRepository,final JpaWebsitesRepository websiteRepository,final JpaUsersRepository userRepository) {
        this.articleRepository = articleRepository;
        this.topicRepository = topicRepository;
        this.websiteRepository = websiteRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getAll() {
        final List<ArticleEntity> articles = articleRepository.findAll();
        final List<ArticleDto> articleDtoList = new ArrayList<>();
        for (final ArticleEntity article : articles){
            final List<TopicEntity> topicEntities = article.getTopics().stream().toList();
            articleDtoList.add(ArticleDto.fromArticle(article, TopicDto.getTopicDtoList(topicEntities)));
        }
        return articleDtoList;
    }

    @Transactional(readOnly = true)
    public ArticleDto findById(final ArticleId articleId) {
        final ArticleEntity article = articleRepository
                .findById(articleId.value())
                .orElseThrow(() -> new ArticleNotFoundException(articleId));
        return ArticleDto.fromArticle(article, TopicDto.getTopicDtoList(article.getTopics().stream().toList()));
    }

    @Transactional(readOnly = true)
    public List<ResponseUserArticleDto> getUserArticles(final UUID userId) {
        final List<UserArticlesEntity> articles = articleRepository.getUserArticles(userId);
        return articles.stream().map(UserArticlesEntity::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getAllUnknown(final UUID userId) {
        final UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(new UserId(userId)));

        final List<ArticleEntity> articles = articleRepository.getAllUnknown(userId);

        final List<ArticleDto> articleDtoList = new ArrayList<>();

        for (final ArticleEntity article : articles){
            final List<TopicEntity> topicEntities = articleRepository.getArticleTopicsForUser(article.getArticleId(),userId).stream().toList();
            articleDtoList.add(ArticleDto.fromArticle(article, TopicDto.getTopicDtoList(topicEntities)));
        }
        return articleDtoList;
    }

    @Transactional
    public void addToKnown(final UUID userId, final UUID articleId) {
        final UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(new UserId(userId)));

        final ArticleEntity article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(new ArticleId(articleId)));

        article.assignArticle(user, Grade.NONE);
    }

    @Transactional
    public List<ArticleDto> getAllUnknownByLikes(final UUID userId){
        final UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(new UserId(userId)));

        final List<ArticleEntity> articles = articleRepository.getAllUnknownByLikes(userId);

        final List<ArticleDto> articleDtoList = new ArrayList<>();

        for (final ArticleEntity article : articles){
            final List<TopicEntity> topicEntities = articleRepository.getArticleTopicsForUser(article.getArticleId(), userId).stream().toList();
            articleDtoList.add(ArticleDto.fromArticle(article, TopicDto.getTopicDtoList(topicEntities)));
        }

        for (final ArticleEntity article : articles) {
            article.assignArticle(user, Grade.NONE);
        }
        return articleDtoList;
    }

    @Transactional(readOnly = true)
    public List<TopicDto> getArticleTopicsForUser(final ArticleId articleId, final UserId userId){
        final UserEntity user = userRepository.findById(userId.value())
                .orElseThrow(() -> new UserNotFoundException(new UserId(userId.value())));
        final ArticleEntity article = articleRepository.findById(articleId.value()).orElseThrow(() -> new ArticleNotFoundException(articleId));
        final List<TopicEntity> topicEntities = articleRepository.getArticleTopicsForUser(articleId.value(), userId.value());
        final List<TopicDto> topicDtoList = new ArrayList<>();
        for (final TopicEntity topic : topicEntities){
            topicDtoList.add(new TopicDto(topic.getName(), new UserId(topic.getCreatorId())));
        }
        return topicDtoList;
    }

    @Transactional()
    public ResponseArticleDto create(final RequestArticleDto articleDto) {
        final WebsiteEntity website = websiteRepository.findById(articleDto.websiteId())
                .orElseThrow(() -> new TopicNotFoundException(String.valueOf(articleDto.websiteId())));
        final ArticleEntity articleEntity = new ArticleEntity(articleDto.title(), articleDto.url(), new Timestamp(System.currentTimeMillis()), website);
        website.addArticle(articleEntity);
        articleRepository.save(articleEntity);

        LOG.debug("Created article with id = {}", articleEntity.getArticleId());
        return new ResponseArticleDto(articleEntity.getArticleId(), articleEntity.getTitle(), articleEntity.getUrl(), articleEntity.getCreatedAt(), ResponseWebsiteDto.fromWebsite(website));
    }

    @Transactional
    public void addTopic(final ArticleId articleId, final TopicId topicId){
        final TopicEntity topic = topicRepository.findById(topicId.value()).orElseThrow(() -> new TopicNotFoundException(topicId));
        final ArticleEntity article = articleRepository.findById(articleId.value()).orElseThrow(() -> new ArticleNotFoundException(articleId));
        article.addTopic(topic);
        articleRepository.save(article);
    }

    @Transactional()
    public void update(final ArticleId articleId, // NOPMD
                       final String title,
                       final String url
    ) {
        articleRepository.update(articleId.value(), title, url);
    }

    @Transactional()
    public void delete(final ArticleId articleId) {
        articleRepository.delete(articleId.value());
    }

    public boolean isArticleWithUrlAdd(final String url){
        return articleRepository.findByUrl(url).isPresent();
    }

    public Optional<ArticleId> findArticleIdByUrl(final String url){
        if (isArticleWithUrlAdd(url)){
            return Optional.of(new ArticleId(articleRepository.findByUrl(url).get().getArticleId()));
        }
        return Optional.empty();
    }
}