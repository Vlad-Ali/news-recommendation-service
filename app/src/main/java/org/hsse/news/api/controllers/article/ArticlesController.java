package org.hsse.news.api.controllers.article;


import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.hsse.news.api.schemas.response.article.ArticleListResponse;
import org.hsse.news.database.article.ArticleResponseMapper;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.dto.ArticleDto;
import org.hsse.news.dto.RequestArticleDto;
import org.hsse.news.dto.ResponseUserArticleDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/articles")
//@RequiredArgsConstructor
@Tag(name = "Article API", description = "Управление статьями")
public class ArticlesController implements ArticleOperations {
    private final ArticlesService articleService;

    public ArticlesController(final ArticlesService articleService) {
        this.articleService = articleService;
    }

    @Override
    public ResponseEntity<List<ArticleDto>> getAll() {
        final List<ArticleDto> articles = articleService.getAll();
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ArticleDto> getArticle(final UUID articleId) {
        final ArticleDto article = articleService.findById(new ArticleId(articleId));
        return ResponseEntity.ok(article);
    }

    @Override
    public ResponseEntity<List<ResponseUserArticleDto>> getUserArticles(final UUID userId) {
        final List<ResponseUserArticleDto> articles = articleService.getUserArticles(userId);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ArticleListResponse> getAllUnknown(final UUID userId) {
        final List<ArticleDto> articles = articleService.getAllUnknown(userId);
        final ArticleListResponse articleListResponse = ArticleResponseMapper.getArticleListResponse(articles);
        return new ResponseEntity<>(articleListResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> createArticle(final RequestArticleDto articleData) {
        articleService.create(articleData);
        return new ResponseEntity<>("Article created", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> updateArticle(final UUID articleId, final ArticleDto articleDto) {
        articleService.update(new ArticleId(articleId), articleDto.title(), articleDto.url());

        return new ResponseEntity<>(articleId.toString(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteArticle(final UUID articleId) {
        articleService.delete(new ArticleId(articleId));
        return new ResponseEntity<>(articleId.toString(), HttpStatus.NO_CONTENT);
    }
}