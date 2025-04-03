package org.hsse.news.api.controllers.userarticle;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.hsse.news.database.article.UserArticlesService;
import org.hsse.news.dto.RequestUserArticleDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "User articles API", description = "Управление связями пользователь-статья")
public class UserArticlesController implements UserArticlesOperations {
    private final UserArticlesService userArticlesService;

    public UserArticlesController(final UserArticlesService userArticlesService) {
        this.userArticlesService = userArticlesService;
    }

    @Override
    public ResponseEntity<Integer> getUserArticlesLikes(final UUID articleId) {
        final Integer count = userArticlesService.getArticleLikeCount(articleId);
        return new  ResponseEntity<>(count, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getUserArticlesDislikes(final UUID articleId) {
        final Integer count = userArticlesService.getArticleDislikeCount(articleId);
        return new  ResponseEntity<>(count, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> createUserArticle(final RequestUserArticleDto createDto) {
        userArticlesService.create(createDto);

        return new ResponseEntity<>("Статья создана", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> likeUserArticle(final RequestUserArticleDto requestUserArticleDto) {
        userArticlesService.likeUserArticle(requestUserArticleDto);
        return new ResponseEntity<>("Лайк поставлен", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> dislikeUserArticle(final RequestUserArticleDto requestUserArticleDto) {
        userArticlesService.dislikeUserArticle(requestUserArticleDto);
        return new ResponseEntity<>("Дизлайк поставлен", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> removeMarkFromUserArticle(final RequestUserArticleDto requestUserArticleDto) {
        userArticlesService.removeMarkFromUserArticle(requestUserArticleDto);
        return new ResponseEntity<>("Отметка убрана", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteUserArticle(final RequestUserArticleDto requestUserArticleDto) {
        userArticlesService.delete(requestUserArticleDto);
        return new ResponseEntity<>("Связь удалена", HttpStatus.NO_CONTENT);
    }
}
