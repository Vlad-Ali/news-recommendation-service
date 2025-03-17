package org.hsse.news.database.userarticles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("api/user-articles")
public interface UserArticlesOperations {

    @GetMapping("/articles/likes")
    @Operation(summary = "Получение количетва положительных оценок статьи")
    @ApiResponse(responseCode = "200", description = "Лайки получены") // NOPMD
    ResponseEntity<Integer> getUserArticlesLikes(@RequestParam("articleId") UUID articleId);

    @GetMapping("/articles/dislikes")
    @Operation(summary = "Получение количетва отрицательных оценок статьи")
    @ApiResponse(responseCode = "200", description = "Дизлайки получены")
    ResponseEntity<Integer> getUserArticlesDislikes(@RequestParam("articleId") UUID articleId);

    @PostMapping("/create")
    @Operation(summary = "Создание связи пользователь-сайт")
    @ApiResponse(responseCode = "201", description = "Связь создана")
    ResponseEntity<String> createUserArticle(@RequestBody UserArticleDto userArticleDto);

    @PatchMapping("/like")
    @Operation(summary = "Поставить положительную оценку")
    @ApiResponse(responseCode = "200", description = "Лайк поставлен")
    ResponseEntity<String> likeUserArticle(@RequestBody UserArticleDto userArticleDto);

    @PatchMapping("/dislike")
    @Operation(summary = "Поставить отрицательную оценку")
    @ApiResponse(responseCode = "200", description = "Дизлайк поставлен")
    ResponseEntity<String> dislikeUserArticle(@RequestBody UserArticleDto userArticleDto);

    @PatchMapping("/unmark")
    @Operation(summary = "Убрать оценку")
    @ApiResponse(responseCode = "200", description = "Отметка убрана")
    ResponseEntity<String> removeMarkFromUserArticle(@RequestBody UserArticleDto userArticleDto);

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить связь пользователь-сайт")
    @ApiResponse(responseCode = "204", description = "Связь удалена")
    ResponseEntity<String> deleteUserArticle(@RequestBody UserArticleDto userArticleDto);
}
