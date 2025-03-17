package org.hsse.news.api.operations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.hsse.news.database.article.models.*;
import org.hsse.news.database.userarticles.UserArticleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/default")
public interface ArticleOperations {

  @GetMapping("/articles")
  @Operation(summary = "Получение всех статей")
  @ApiResponse(responseCode = "200", description = "Статьи получены")
  ResponseEntity<List<ArticleDto>> getAll();

  @GetMapping("/articles/{userId}")
  @Operation(summary = "Получение статей пользователя по ID")
  @ApiResponse(responseCode = "200", description = "Статьи получены")
  ResponseEntity<List<UserArticleDto>> getUserArticles(
      @Parameter(description = "ID пользователя")
      @PathVariable("userId") UUID userId);

  @GetMapping("/{articleId}")
  @Operation(summary = "Получение статьи по ее ID")
  @ApiResponse(responseCode = "200", description = "Статья найдена")
  ResponseEntity<ArticleDto> getArticle(@Parameter(description = "ID статьи") @PathVariable UUID articleId);

  @PostMapping("/create")
  @Operation(summary = "Создание статьи")
  @ApiResponse(responseCode = "201", description = "Статья создана")
  ResponseEntity<String> createArticle(@RequestBody ArticleDto articleData);

  @PutMapping("/update/{articleId}")
  @Operation(summary = "Обновление статьи")
  @ApiResponse(responseCode = "201", description = "Статья обновлена")
  ResponseEntity<String> updateArticle(
      @Parameter(description = "ID статьи") @PathVariable UUID articleId,
      @RequestBody ArticleDto articleData
  );

  @DeleteMapping("/delete/{articleId}")
  @Operation(summary = "Удаление статьи")
  @ApiResponse(responseCode = "204", description = "Статья удалена")
  ResponseEntity<String> deleteArticle(@Parameter(description = "ID статьи") UUID articleId);
}