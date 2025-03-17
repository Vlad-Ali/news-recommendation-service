package org.hsse.news.database.userarticles;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Tag(name = "User articles API", description = "Управление связями пользователь-статья")
public class UserArticlesController implements UserArticlesOperations {

    private final UserArticlesService userArticlesService;

    @Override
    public ResponseEntity<Integer> getUserArticlesLikes(UUID articleId) {
        Integer count = userArticlesService.getArticleLikeCount(articleId);
        return new  ResponseEntity<>(count, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getUserArticlesDislikes(UUID articleId) {
        Integer count = userArticlesService.getArticleDislikeCount(articleId);
        return new  ResponseEntity<>(count, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> createUserArticle(UserArticleDto userArticleDto) {
        userArticlesService.create(userArticleDto);

        return new ResponseEntity<>("Статья создана", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> likeUserArticle(UserArticleDto userArticleDto) {
        userArticlesService.likeUserArticle(userArticleDto);
        return new ResponseEntity<>("Лайк поставлен", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> dislikeUserArticle(UserArticleDto userArticleDto) {
        userArticlesService.dislikeUserArticle(userArticleDto);
        return new ResponseEntity<>("Дизлайк поставлен", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> removeMarkFromUserArticle(UserArticleDto userArticleDto) {
        userArticlesService.removeMarkFromUserArticle(userArticleDto);
        return new ResponseEntity<>("Отметка убрана", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteUserArticle(UserArticleDto userArticleDto) {
        userArticlesService.delete(userArticleDto);
        return new ResponseEntity<>("Связь удалена", HttpStatus.NO_CONTENT);
    }
}
