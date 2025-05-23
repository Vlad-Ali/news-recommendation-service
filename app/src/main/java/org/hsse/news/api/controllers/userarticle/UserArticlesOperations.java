package org.hsse.news.api.controllers.userarticle;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.hsse.news.dto.RequestUserArticleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("api/articles")
public interface UserArticlesOperations {

    @GetMapping("/{articleId}/likes")
    @Operation(summary = "Получение количетва положительных оценок статьи")
    @ApiResponse(responseCode = "200", description = "Лайки получены") // NOPMD
    ResponseEntity<Integer> getUserArticlesLikes(@RequestParam("articleId") UUID articleId);

    @GetMapping("/{articleId}/dislikes")
    @Operation(summary = "Получение количетва отрицательных оценок статьи")
    @ApiResponse(responseCode = "200", description = "Дизлайки получены")
    ResponseEntity<Integer> getUserArticlesDislikes(@RequestParam("articleId") UUID articleId);

    @PostMapping("/")
    @Operation(summary = "Создание связи пользователь-сайт")
    @ApiResponse(responseCode = "201", description = "Связь создана")
    ResponseEntity<String> createUserArticle(@RequestBody RequestUserArticleDto createDto);

    @PatchMapping("/{articleId}:like")
    @Operation(summary = "Поставить положительную оценку")
    @ApiResponse(responseCode = "200", description = "Лайк поставлен")
    ResponseEntity<String> likeUserArticle(@RequestBody RequestUserArticleDto responseUserArticleDto);

    @PatchMapping("/{articleId}:dislike")
    @Operation(summary = "Поставить отрицательную оценку")
    @ApiResponse(responseCode = "200", description = "Дизлайк поставлен")
    ResponseEntity<String> dislikeUserArticle(@RequestBody RequestUserArticleDto responseUserArticleDto);

    @PatchMapping("/{articleId}:unmark")
    @Operation(summary = "Убрать оценку")
    @ApiResponse(responseCode = "200", description = "Отметка убрана")
    ResponseEntity<String> removeMarkFromUserArticle(@RequestBody RequestUserArticleDto responseUserArticleDto);

    @DeleteMapping("/")
    @Operation(summary = "Удалить связь пользователь-сайт")
    @ApiResponse(responseCode = "204", description = "Связь удалена")
    ResponseEntity<String> deleteUserArticle(@RequestBody RequestUserArticleDto responseUserArticleDto);
}
